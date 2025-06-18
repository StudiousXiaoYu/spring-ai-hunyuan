/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.studiousxiaoyu.hunyuan;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.model.*;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi.*;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi.ChatCompletion.*;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi.ChatCompletionMessage.*;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import io.github.studiousxiaoyu.hunyuan.metadata.HunYuanUsage;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.*;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HunYuanChatModel is a {@link ChatModel} implementation that uses the HunYuan
 *
 * @author Guo Junyu
 */
public class HunYuanChatModel implements ChatModel, StreamingChatModel {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanChatModel.class);

	private static final ToolCallingManager DEFAULT_TOOL_CALLING_MANAGER = ToolCallingManager.builder().build();

	private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();

	/**
	 * The default options used for the chat completion requests.
	 */
	private final HunYuanChatOptions defaultOptions;

	/**
	 * Low-level access to the HunYuan API.
	 */
	private final HunYuanApi hunYuanApi;

	private final RetryTemplate retryTemplate;

	/**
	 * Observation registry used for instrumentation.
	 */
	private final ObservationRegistry observationRegistry;

	private final ToolCallingManager toolCallingManager;

	private final ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate;

	/**
	 * Conventions to use for generating observations.
	 */
	private ChatModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	/**
	 * Initializes a new instance of the HunYuanChatModel.
	 * @param hunYuanApi The HunYuan instance to be used for interacting with the HunYuan
	 * Chat API.
	 * @param options The HunYuanChatOptions to configure the chat client. function by its
	 * name.
	 * @param retryTemplate The retry template.
	 */
	public HunYuanChatModel(HunYuanApi hunYuanApi, HunYuanChatOptions options, ToolCallingManager toolCallingManager,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry) {
		this(hunYuanApi, options, toolCallingManager, retryTemplate, observationRegistry,
				new DefaultToolExecutionEligibilityPredicate());
	}

	/**
	 * Initializes a new instance of the HunYuanChatModel.
	 * @param hunYuanApi The HunYuan instance to be used for interacting with the HunYuan
	 * Chat API.
	 * @param options The HunYuanChatOptions to configure the chat client.
	 * @param retryTemplate The retry template.
	 * @param observationRegistry The ObservationRegistry used for instrumentation.
	 */
	public HunYuanChatModel(HunYuanApi hunYuanApi, HunYuanChatOptions options, ToolCallingManager toolCallingManager,
			RetryTemplate retryTemplate, ObservationRegistry observationRegistry,
			ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
		Assert.notNull(hunYuanApi, "HunYuanApi must not be null");
		Assert.notNull(options, "Options must not be null");
		Assert.notNull(toolCallingManager, "toolCallingManager cannot be null");
		Assert.notNull(retryTemplate, "RetryTemplate must not be null");
		Assert.notNull(observationRegistry, "ObservationRegistry must not be null");
		Assert.notNull(toolExecutionEligibilityPredicate, "toolExecutionEligibilityPredicate cannot be null");
		this.hunYuanApi = hunYuanApi;
		this.defaultOptions = options;
		this.toolCallingManager = toolCallingManager;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
		this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
	}

	private static Generation buildGeneration(Choice choice, Map<String, Object> metadata) {
		List<AssistantMessage.ToolCall> toolCalls = choice.message().toolCalls() == null ? List.of()
				: choice.message()
					.toolCalls()
					.stream()
					.map(toolCall -> new AssistantMessage.ToolCall(toolCall.id(), "function",
							toolCall.function().name(), toolCall.function().arguments()))
					.toList();

		var assistantMessage = new AssistantMessage(choice.message().content(), metadata, toolCalls);
		String finishReason = (choice.finishReason() != null ? choice.finishReason() : "");
		var generationMetadata = ChatGenerationMetadata.builder().finishReason(finishReason).build();
		return new Generation(assistantMessage, generationMetadata);
	}

	Prompt buildRequestPrompt(Prompt prompt) {
		// Process runtime options
		HunYuanChatOptions runtimeOptions = null;
		if (prompt.getOptions() != null) {
			if (prompt.getOptions() instanceof ToolCallingChatOptions toolCallingChatOptions) {
				runtimeOptions = ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class,
						HunYuanChatOptions.class);
			}
			else {
				runtimeOptions = ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class,
						HunYuanChatOptions.class);
			}
		}

		// Define request options by merging runtime options and default options
		HunYuanChatOptions requestOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions,
				HunYuanChatOptions.class);

		// Merge @JsonIgnore-annotated options explicitly since they are ignored by
		// Jackson, used by ModelOptionsUtils.
		if (runtimeOptions != null) {
			if (runtimeOptions.getTopK() != null) {
				logger.warn("The topK option is not supported by OpenAI chat models. Ignoring.");
			}

			requestOptions.setHttpHeaders(
					mergeHttpHeaders(runtimeOptions.getHttpHeaders(), this.defaultOptions.getHttpHeaders()));
			requestOptions.setInternalToolExecutionEnabled(
					ModelOptionsUtils.mergeOption(runtimeOptions.getInternalToolExecutionEnabled(),
							this.defaultOptions.getInternalToolExecutionEnabled()));
			requestOptions.setToolNames(ToolCallingChatOptions.mergeToolNames(runtimeOptions.getToolNames(),
					this.defaultOptions.getToolNames()));
			requestOptions.setToolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(runtimeOptions.getToolCallbacks(),
					this.defaultOptions.getToolCallbacks()));
			requestOptions.setToolContext(ToolCallingChatOptions.mergeToolContext(runtimeOptions.getToolContext(),
					this.defaultOptions.getToolContext()));
		}
		else {
			requestOptions.setHttpHeaders(this.defaultOptions.getHttpHeaders());
			requestOptions.setInternalToolExecutionEnabled(this.defaultOptions.getInternalToolExecutionEnabled());
			requestOptions.setToolNames(this.defaultOptions.getToolNames());
			requestOptions.setToolCallbacks(this.defaultOptions.getToolCallbacks());
			requestOptions.setToolContext(this.defaultOptions.getToolContext());
		}

		ToolCallingChatOptions.validateToolCallbacks(requestOptions.getToolCallbacks());

		return new Prompt(prompt.getInstructions(), requestOptions);
	}

	private Map<String, String> mergeHttpHeaders(Map<String, String> runtimeHttpHeaders,
			Map<String, String> defaultHttpHeaders) {
		var mergedHttpHeaders = new HashMap<>(defaultHttpHeaders);
		mergedHttpHeaders.putAll(runtimeHttpHeaders);
		return mergedHttpHeaders;
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		Prompt requestPrompt = buildRequestPrompt(prompt);
		return this.internalCall(requestPrompt, null);
	}

	public ChatResponse internalCall(Prompt prompt, ChatResponse previousChatResponse) {
		ChatCompletionRequest request = createRequest(prompt, false);

		ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
			.prompt(prompt)
			.provider(HunYuanConstants.PROVIDER_NAME)
			.build();

		ChatResponse response = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION
			.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry)
			.observe(() -> {
				ResponseEntity<ChatCompletionResponse> completionEntity = this.retryTemplate
					.execute(ctx -> this.hunYuanApi.chatCompletionEntity(request));

				var chatCompletion = completionEntity.getBody().response();

				if (chatCompletion == null) {
					logger.warn("No chat completion returned for prompt: {}", prompt);
					return new ChatResponse(List.of());
				}

				List<Choice> choices = chatCompletion.choices();
				if (choices == null) {
					logger.warn("No choices returned for prompt: {}", prompt);
					return new ChatResponse(List.of());
				}

				List<Generation> generations = choices.stream().map(choice -> {
			// @formatter:off
						Map<String, Object> metadata = Map.of(
								"id", chatCompletion.id(),
								"role", choice.message().role() != null ? choice.message().role().name() : "",
								"finishReason", choice.finishReason() != null ? choice.finishReason() : ""
						);
						// @formatter:on
					return buildGeneration(choice, metadata);
				}).toList();

				ChatResponse chatResponse = new ChatResponse(generations,
						from(request, completionEntity.getBody().response()));

				observationContext.setResponse(chatResponse);

				return chatResponse;
			});

		if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
			var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
			if (toolExecutionResult.returnDirect()) {
				// Return tool execution result directly to the client.
				return ChatResponse.builder()
					.from(response)
					.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
					.build();
			}
			else {
				// Send the tool execution result back to the model.
				return this.internalCall(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
						response);
			}
		}
		return response;
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return this.defaultOptions.copy();
	}

	@Override
	public Flux<ChatResponse> stream(Prompt prompt) {
		Prompt requestPrompt = buildRequestPrompt(prompt);
		return internalStream(requestPrompt, null);
	}

	public Flux<ChatResponse> internalStream(Prompt prompt, ChatResponse previousChatResponse) {
		return Flux.deferContextual(contextView -> {
			ChatCompletionRequest request = createRequest(prompt, true);

			Flux<ChatCompletionChunk> completionChunks = this.retryTemplate
				.execute(ctx -> this.hunYuanApi.chatCompletionStream(request));

			// For chunked responses, only the first chunk contains the choice role.
			// The rest of the chunks with same ID share the same role.
			ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

			final ChatModelObservationContext observationContext = ChatModelObservationContext.builder()
				.prompt(prompt)
				.provider(HunYuanConstants.PROVIDER_NAME)
				.build();

			Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(
					this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
					this.observationRegistry);

			observation.parentObservation(contextView.getOrDefault(ObservationThreadLocalAccessor.KEY, null)).start();

			// Convert the ChatCompletionChunk into a ChatCompletion to be able to reuse
			// the function call handling logic.
			Flux<ChatResponse> chatResponse = completionChunks.map(this::chunkToChatCompletion)
				.switchMap(chatCompletion -> Mono.just(chatCompletion).map(chatCompletion2 -> {
					try {
						String id = chatCompletion2.id();

						List<Generation> generations = chatCompletion2.choices().stream().map(choice -> {
							if (choice.message().role() != null) {
								roleMap.putIfAbsent(id, choice.message().role().name());
							}

				// @formatter:off
								Map<String, Object> metadata = Map.of(
										"id", chatCompletion2.id(),
										"role", roleMap.getOrDefault(id, ""),
										"finishReason", choice.finishReason() != null ? choice.finishReason() : ""
								);
								// @formatter:on
							return buildGeneration(choice, metadata);
						}).toList();

						return new ChatResponse(generations, from(request, chatCompletion2));
					}
					catch (Exception e) {
						logger.error("Error processing chat completion", e);
						return new ChatResponse(List.of());
					}

				}));

			Flux<ChatResponse> flux = chatResponse.flatMap(response -> {
				if (this.toolExecutionEligibilityPredicate.isToolExecutionRequired(prompt.getOptions(), response)) {
					return Flux.defer(() -> {
						var toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
						if (toolExecutionResult.returnDirect()) {
							// Return tool execution result directly to the client.
							return Flux.just(ChatResponse.builder()
								.from(response)
								.generations(ToolExecutionResult.buildGenerations(toolExecutionResult))
								.build());
						}
						else {
							// Send the tool execution result back to the model.
							return this.internalStream(
									new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()),
									response);
						}
					}).subscribeOn(Schedulers.boundedElastic());
				}
				else {
					return Flux.just(response);
				}
			})
				.doOnError(observation::error)
				.doFinally(signalType -> observation.stop())
				.contextWrite(ctx -> ctx.put(ObservationThreadLocalAccessor.KEY, observation));

			return new MessageAggregator().aggregate(flux, observationContext::setResponse);
		});
	}

	private ChatResponseMetadata from(ChatCompletionRequest request, ChatCompletion result) {
		Assert.notNull(result, "HunYuan ChatCompletionResult must not be null");
		return ChatResponseMetadata.builder()
			.id(result.id() != null ? result.id() : "")
			.usage(result.usage() != null ? HunYuanUsage.from(result.usage()) : new EmptyUsage())
			.model(request.model() != null ? request.model() : "")
			.keyValue("created", result.created() != null ? result.created() : 0L)
			.build();
	}

	/**
	 * Convert the ChatCompletionChunk into a ChatCompletion. The Usage is set to null.
	 * @param chunk the ChatCompletionChunk to convert
	 * @return the ChatCompletion
	 */
	private ChatCompletion chunkToChatCompletion(ChatCompletionChunk chunk) {
		List<ChatCompletion.Choice> choices = chunk.choices().stream().map(chunkChoice -> {
			ChatCompletionMessage chatCompletionMessage = null;
			ChatCompletionDelta delta = chunkChoice.delta();
			if (delta == null) {
				chatCompletionMessage = new ChatCompletionMessage("", Role.assistant);
			}
			else {
				chatCompletionMessage = new ChatCompletionMessage(delta.content(), delta.role(), delta.toolCalls());
			}
			return new ChatCompletion.Choice(chunkChoice.index(), chatCompletionMessage, chunkChoice.finishReason(),
					delta);
		}).toList();

		return new ChatCompletion(chunk.id(), chunk.errorMsg(), chunk.created(), chunk.note(), choices, chunk.usage(),
				chunk.moderationLevel(), chunk.searchInfo(), chunk.replaces(), chunk.recommendedQuestions(),
				chunk.requestId());
	}

	/**
	 * Accessible for testing.
	 */
	public HunYuanApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
		List<ChatCompletionMessage> systemMessages = new ArrayList<>();
		List<ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().filter(message -> {
			if (message.getMessageType() == MessageType.SYSTEM) {
				Object content = message.getText();
				systemMessages.add(new ChatCompletionMessage(content, Role.system));
				return false;
			}
			return true;
		}).map(message -> {
			if (message.getMessageType() == MessageType.USER) {
				Object content = message.getText();
				if (message instanceof UserMessage userMessage) {
					if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
						List<ChatContent> contentList = new ArrayList<>(List.of(new ChatContent(message.getText())));

						contentList.addAll(userMessage.getMedia()
							.stream()
							.map(media -> new ChatContent(
									new ImageUrl(this.fromMediaData(media.getMimeType(), media.getData()))))
							.toList());
						return List.of(new ChatCompletionMessage(Role.user, contentList));
					}
				}
				return List.of(new ChatCompletionMessage(content, Role.user));
			}
			else if (message.getMessageType() == MessageType.ASSISTANT) {
				var assistantMessage = (AssistantMessage) message;
				List<ToolCall> toolCalls = null;
				if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
					toolCalls = assistantMessage.getToolCalls().stream().map(toolCall -> {
						var function = new ChatCompletionFunction(toolCall.name(), toolCall.arguments());
						return new ToolCall(toolCall.id(), toolCall.type(), null, function);
					}).toList();
				}
				return List.of(new ChatCompletionMessage(assistantMessage.getText(),
						ChatCompletionMessage.Role.assistant, null, null, toolCalls));
			}
			else if (message.getMessageType() == MessageType.TOOL) {
				ToolResponseMessage toolMessage = (ToolResponseMessage) message;

				toolMessage.getResponses()
					.forEach(response -> Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id"));

				return toolMessage.getResponses()
					.stream()
					.map(tr -> new ChatCompletionMessage(tr.responseData(), ChatCompletionMessage.Role.tool, null,
							tr.id(), null))
					.toList();
			}
			else {
				throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
			}
		}).flatMap(List::stream).collect(Collectors.toList());
		systemMessages.stream().forEach(systemMessage -> {
			chatCompletionMessages.add(0, systemMessage);
		});
		ChatCompletionRequest request = new ChatCompletionRequest(chatCompletionMessages, stream);

		HunYuanChatOptions requestOptions = (HunYuanChatOptions) prompt.getOptions();
		request = ModelOptionsUtils.merge(requestOptions, request, ChatCompletionRequest.class);

		// Add the tool definitions to the request's tools parameter.
		List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);
		if (!CollectionUtils.isEmpty(toolDefinitions)) {
			request = ModelOptionsUtils.merge(
					HunYuanChatOptions.builder().tools(this.getFunctionTools(toolDefinitions)).build(), request,
					ChatCompletionRequest.class);
		}

		return request;
	}

	private String fromMediaData(MimeType mimeType, Object mediaContentData) {
		if (mediaContentData instanceof byte[] bytes) {
			// Assume the bytes are an image. So, convert the bytes to a base64 encoded
			// following the prefix pattern.
			return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
		}
		else if (mediaContentData instanceof String text) {
			// Assume the text is a URLs or a base64 encoded image prefixed by the user.
			return text;
		}
		else {
			throw new IllegalArgumentException(
					"Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
		}
	}

	private ChatOptions buildRequestOptions(HunYuanApi.ChatCompletionRequest request) {
		return ChatOptions.builder()
			.model(request.model())
			// .frequencyPenalty(request.frequencyPenalty())
			// .maxTokens(request.maxTokens())
			// .presencePenalty(request.presencePenalty())
			.stopSequences(request.stop())
			.temperature(request.temperature())
			.topP(request.topP())
			.build();
	}

	private List<HunYuanApi.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
		return toolDefinitions.stream().map(toolDefinition -> {
			var function = new HunYuanApi.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(),
					toolDefinition.inputSchema());
			return new HunYuanApi.FunctionTool(function);
		}).toList();
	}

	public void setObservationConvention(ChatModelObservationConvention observationConvention) {
		this.observationConvention = observationConvention;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns a builder pre-populated with the current configuration for mutation.
	 */
	public Builder mutate() {
		return new Builder(this);
	}

	@Override
	public HunYuanChatModel clone() {
		return this.mutate().build();
	}

	public static final class Builder {

		// Copy constructor for mutate()
		public Builder(HunYuanChatModel model) {
			this.hunYuanApi = model.hunYuanApi;
			this.defaultOptions = model.defaultOptions;
			this.toolCallingManager = model.toolCallingManager;
			this.toolExecutionEligibilityPredicate = model.toolExecutionEligibilityPredicate;
			this.retryTemplate = model.retryTemplate;
			this.observationRegistry = model.observationRegistry;
		}

		private HunYuanApi hunYuanApi;

		private HunYuanChatOptions defaultOptions = HunYuanChatOptions.builder()
			.model(HunYuanApi.DEFAULT_CHAT_MODEL)
			.temperature(0.7)
			.build();

		private ToolCallingManager toolCallingManager;

		private ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate = new DefaultToolExecutionEligibilityPredicate();

		private RetryTemplate retryTemplate = RetryUtils.DEFAULT_RETRY_TEMPLATE;

		private ObservationRegistry observationRegistry = ObservationRegistry.NOOP;

		private Builder() {
		}

		public Builder hunYuanApi(HunYuanApi hunYuanApi) {
			this.hunYuanApi = hunYuanApi;
			return this;
		}

		public Builder defaultOptions(HunYuanChatOptions defaultOptions) {
			this.defaultOptions = defaultOptions;
			return this;
		}

		public Builder toolCallingManager(ToolCallingManager toolCallingManager) {
			this.toolCallingManager = toolCallingManager;
			return this;
		}

		public Builder toolExecutionEligibilityPredicate(
				ToolExecutionEligibilityPredicate toolExecutionEligibilityPredicate) {
			this.toolExecutionEligibilityPredicate = toolExecutionEligibilityPredicate;
			return this;
		}

		public Builder retryTemplate(RetryTemplate retryTemplate) {
			this.retryTemplate = retryTemplate;
			return this;
		}

		public Builder observationRegistry(ObservationRegistry observationRegistry) {
			this.observationRegistry = observationRegistry;
			return this;
		}

		public HunYuanChatModel build() {
			if (this.toolCallingManager != null) {
				return new HunYuanChatModel(this.hunYuanApi, this.defaultOptions, this.toolCallingManager,
						this.retryTemplate, this.observationRegistry, this.toolExecutionEligibilityPredicate);
			}
			return new HunYuanChatModel(this.hunYuanApi, this.defaultOptions, DEFAULT_TOOL_CALLING_MANAGER,
					this.retryTemplate, this.observationRegistry, this.toolExecutionEligibilityPredicate);
		}

	}

}
