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

package io.github.studiousxiaoyu.hunyuan.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.prompt.ChatOptions;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.*;

/**
 * Options for HunYuan chat completions.
 *
 * @author Guo Junyu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HunYuanChatOptions implements ToolCallingChatOptions {

	private @JsonProperty("Model") String model;

	private @JsonProperty("Temperature") Double temperature;

	private @JsonProperty("TopP") Double topP;

	private @JsonProperty("Seed") Integer seed;

	private @JsonProperty("EnableEnhancement") Boolean enableEnhancement;

	private @JsonProperty("StreamModeration") Boolean streamModeration;

	private @JsonProperty("Stop") List<String> stop;

	private @JsonProperty("Tools") List<HunYuanApi.FunctionTool> tools;

	private @JsonProperty("ToolChoice") String toolChoice;

	private @JsonProperty("CustomTool") HunYuanApi.FunctionTool customTool;

	private @JsonProperty("SearchInfo") Boolean searchInfo;

	private @JsonProperty("Citation") Boolean citation;

	private @JsonProperty("EnableSpeedSearch") Boolean enableSpeedSearch;

	private @JsonProperty("EnableMultimedia") Boolean enableMultimedia;

	private @JsonProperty("EnableDeepSearch") Boolean enableDeepSearch;

	private @JsonProperty("forceSearchEnhancement") Boolean forceSearchEnhancement;

	private @JsonProperty("EnableRecommendedQuestions") Boolean enableRecommendedQuestions;

	private @JsonProperty("EnableDeepRead") Boolean enableDeepRead;

	private @JsonProperty("EnableThinking") Boolean enableThinking;

	/**
	 * HunYuan Tool Function Callbacks to register with the ChatModel. For Prompt Options
	 * the functionCallbacks are automatically enabled for the duration of the prompt
	 * execution. For Default Options the functionCallbacks are registered but disabled by
	 * default. Use the enableFunctions to set the functions from the registry to be used
	 * by the ChatModel chat completion requests.
	 */
	@JsonIgnore
	private List<ToolCallback> toolCallbacks = new ArrayList<>();

	/**
	 * Collection of tool names to be resolved at runtime and used for tool calling in the
	 * chat completion requests.
	 */
	@JsonIgnore
	private Set<String> toolNames = new HashSet<>();

	/**
	 * Whether to enable the tool execution lifecycle internally in ChatModel.
	 */
	@JsonIgnore
	private Boolean internalToolExecutionEnabled;

	/**
	 * Optional HTTP headers to be added to the chat completion request.
	 */
	@JsonIgnore
	private Map<String, String> httpHeaders = new HashMap<>();

	@JsonIgnore
	private Map<String, Object> toolContext = new HashMap<>();

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	@Override
	public Double getFrequencyPenalty() {
		return null;
	}

	@Override
	public Integer getMaxTokens() {
		return null;
	}

	@Override
	public Double getPresencePenalty() {
		return null;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getSeed() {
		return this.seed;
	}

	public void setSeed(Integer seed) {
		this.seed = seed;
	}

	@Override
	@JsonIgnore
	public List<String> getStopSequences() {
		return getStop();
	}

	@JsonIgnore
	public void setStopSequences(List<String> stopSequences) {
		setStop(stopSequences);
	}

	public List<String> getStop() {
		return this.stop;
	}

	public void setStop(List<String> stop) {
		this.stop = stop;
	}

	@Override
	public Double getTemperature() {
		return this.temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	@Override
	public Double getTopP() {
		return this.topP;
	}

	@Override
	public <T extends ChatOptions> T copy() {
		return null;
	}

	public void setTopP(Double topP) {
		this.topP = topP;
	}

	public Boolean getEnableEnhancement() {
		return enableEnhancement;
	}

	public void setEnableEnhancement(Boolean enableEnhancement) {
		this.enableEnhancement = enableEnhancement;
	}

	public Boolean getStreamModeration() {
		return streamModeration;
	}

	public void setStreamModeration(Boolean streamModeration) {
		this.streamModeration = streamModeration;
	}

	public List<HunYuanApi.FunctionTool> getTools() {
		return tools;
	}

	public void setTools(List<HunYuanApi.FunctionTool> tools) {
		this.tools = tools;
	}

	public String getToolChoice() {
		return toolChoice;
	}

	public void setToolChoice(String toolChoice) {
		this.toolChoice = toolChoice;
	}

	public HunYuanApi.FunctionTool getCustomTool() {
		return customTool;
	}

	public void setCustomTool(HunYuanApi.FunctionTool customTool) {
		this.customTool = customTool;
	}

	public Boolean getSearchInfo() {
		return searchInfo;
	}

	public void setSearchInfo(Boolean searchInfo) {
		this.searchInfo = searchInfo;
	}

	public Boolean getCitation() {
		return citation;
	}

	public void setCitation(Boolean citation) {
		this.citation = citation;
	}

	public Boolean getEnableSpeedSearch() {
		return enableSpeedSearch;
	}

	public void setEnableSpeedSearch(Boolean enableSpeedSearch) {
		this.enableSpeedSearch = enableSpeedSearch;
	}

	public Boolean getEnableMultimedia() {
		return enableMultimedia;
	}

	public void setEnableMultimedia(Boolean enableMultimedia) {
		this.enableMultimedia = enableMultimedia;
	}

	public Boolean getEnableDeepSearch() {
		return enableDeepSearch;
	}

	public void setEnableDeepSearch(Boolean enableDeepSearch) {
		this.enableDeepSearch = enableDeepSearch;
	}

	public Boolean getForceSearchEnhancement() {
		return forceSearchEnhancement;
	}

	public void setForceSearchEnhancement(Boolean forceSearchEnhancement) {
		forceSearchEnhancement = forceSearchEnhancement;
	}

	public Boolean getEnableRecommendedQuestions() {
		return enableRecommendedQuestions;
	}

	public void setEnableRecommendedQuestions(Boolean enableRecommendedQuestions) {
		this.enableRecommendedQuestions = enableRecommendedQuestions;
	}

	public Boolean getEnableThinking() {
		return enableThinking;
	}

	public void setEnableThinking(Boolean enableThinking) {
		enableThinking = enableThinking;
	}

	public Boolean getEnableDeepRead() {
		return enableDeepRead;
	}

	public void setEnableDeepRead(Boolean enableDeepRead) {
		this.enableDeepRead = enableDeepRead;
	}

	@Override
	@JsonIgnore
	public Integer getTopK() {
		return null;
	}

	@Override
	public Map<String, Object> getToolContext() {
		return this.toolContext;
	}

	@Override
	public void setToolContext(Map<String, Object> toolContext) {
		this.toolContext = toolContext;
	}

	@Override
	@JsonIgnore
	public List<ToolCallback> getToolCallbacks() {
		return toolCallbacks;
	}

	@Override
	@JsonIgnore
	public void setToolCallbacks(List<ToolCallback> toolCallbacks) {
		Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
		Assert.noNullElements(toolCallbacks, "toolCallbacks cannot contain null elements");
		this.toolCallbacks = toolCallbacks;
	}

	@Override
	@JsonIgnore
	public Set<String> getToolNames() {
		return toolNames;
	}

	@Override
	@JsonIgnore
	public void setToolNames(Set<String> toolNames) {
		Assert.notNull(toolNames, "toolNames cannot be null");
		Assert.noNullElements(toolNames, "toolNames cannot contain null elements");
		toolNames.forEach(tool -> Assert.hasText(tool, "toolNames cannot contain empty elements"));
		this.toolNames = toolNames;
	}

	@Override
	@JsonIgnore
	public Boolean getInternalToolExecutionEnabled() {
		return internalToolExecutionEnabled;
	}

	@Override
	@JsonIgnore
	public void setInternalToolExecutionEnabled(Boolean internalToolExecutionEnabled) {
		this.internalToolExecutionEnabled = internalToolExecutionEnabled;
	}

	public Map<String, String> getHttpHeaders() {
		return httpHeaders;
	}

	public void setHttpHeaders(Map<String, String> httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof HunYuanChatOptions that))
			return false;

		return Objects.equals(model, that.model) && Objects.equals(temperature, that.temperature)
				&& Objects.equals(topP, that.topP) && Objects.equals(seed, that.seed)
				&& Objects.equals(enableEnhancement, that.enableEnhancement)
				&& Objects.equals(streamModeration, that.streamModeration) && Objects.equals(stop, that.stop)
				&& Objects.equals(tools, that.tools) && Objects.equals(toolChoice, that.toolChoice)
				&& Objects.equals(customTool, that.customTool) && Objects.equals(searchInfo, that.searchInfo)
				&& Objects.equals(citation, that.citation) && Objects.equals(enableSpeedSearch, that.enableSpeedSearch)
				&& Objects.equals(enableMultimedia, that.enableMultimedia)
				&& Objects.equals(enableDeepSearch, that.enableDeepSearch)
				&& Objects.equals(forceSearchEnhancement, that.forceSearchEnhancement)
				&& Objects.equals(enableRecommendedQuestions, that.enableRecommendedQuestions)
				&& Objects.equals(toolCallbacks, that.toolCallbacks) && Objects.equals(toolNames, that.toolNames)
				&& Objects.equals(internalToolExecutionEnabled, that.internalToolExecutionEnabled)
				&& Objects.equals(enableThinking, that.enableThinking)
				&& Objects.equals(enableDeepRead, that.enableDeepRead) && Objects.equals(httpHeaders, that.httpHeaders)
				&& Objects.equals(toolContext, that.toolContext);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(model);
		result = 31 * result + Objects.hashCode(temperature);
		result = 31 * result + Objects.hashCode(topP);
		result = 31 * result + Objects.hashCode(seed);
		result = 31 * result + Objects.hashCode(enableEnhancement);
		result = 31 * result + Objects.hashCode(streamModeration);
		result = 31 * result + Objects.hashCode(stop);
		result = 31 * result + Objects.hashCode(tools);
		result = 31 * result + Objects.hashCode(toolChoice);
		result = 31 * result + Objects.hashCode(customTool);
		result = 31 * result + Objects.hashCode(searchInfo);
		result = 31 * result + Objects.hashCode(citation);
		result = 31 * result + Objects.hashCode(enableSpeedSearch);
		result = 31 * result + Objects.hashCode(enableMultimedia);
		result = 31 * result + Objects.hashCode(enableDeepSearch);
		result = 31 * result + Objects.hashCode(forceSearchEnhancement);
		result = 31 * result + Objects.hashCode(enableRecommendedQuestions);
		result = 31 * result + Objects.hashCode(toolCallbacks);
		result = 31 * result + Objects.hashCode(toolNames);
		result = 31 * result + Objects.hashCode(internalToolExecutionEnabled);
		result = 31 * result + Objects.hashCode(httpHeaders);
		result = 31 * result + Objects.hashCode(toolContext);
		result = 31 * result + Objects.hashCode(enableThinking);
		result = 31 * result + Objects.hashCode(enableDeepRead);
		return result;
	}

	@Override
	public String toString() {
		return "HunYuanChatOptions: " + ModelOptionsUtils.toJsonString(this);
	}

	public static class Builder {

		protected HunYuanChatOptions options;

		public Builder() {
			this.options = new HunYuanChatOptions();
		}

		public Builder(HunYuanChatOptions options) {
			this.options = options;
		}

		public Builder model(String model) {
			this.options.model = model;
			return this;
		}

		public Builder model(HunYuanApi.ChatModel hunYuanChatModel) {
			this.options.model = hunYuanChatModel.getName();
			return this;
		}

		public Builder seed(Integer seed) {
			this.options.seed = seed;
			return this;
		}

		public Builder stop(List<String> stop) {
			this.options.stop = stop;
			return this;
		}

		public Builder temperature(Double temperature) {
			this.options.temperature = temperature;
			return this;
		}

		public Builder topP(Double topP) {
			this.options.topP = topP;
			return this;
		}

		public Builder tools(List<HunYuanApi.FunctionTool> tools) {
			this.options.tools = tools;
			return this;
		}

		public Builder toolChoice(String toolChoice) {
			this.options.toolChoice = toolChoice;
			return this;
		}

		public Builder toolCallbacks(List<ToolCallback> toolCallbacks) {
			this.options.setToolCallbacks(toolCallbacks);
			return this;
		}

		public Builder toolCallbacks(ToolCallback... toolCallbacks) {
			Assert.notNull(toolCallbacks, "toolCallbacks cannot be null");
			this.options.toolCallbacks.addAll(Arrays.asList(toolCallbacks));
			return this;
		}

		public Builder toolNames(Set<String> toolNames) {
			Assert.notNull(toolNames, "toolNames cannot be null");
			this.options.setToolNames(toolNames);
			return this;
		}

		public Builder toolNames(String... toolNames) {
			Assert.notNull(toolNames, "toolNames cannot be null");
			this.options.toolNames.addAll(Set.of(toolNames));
			return this;
		}

		public Builder internalToolExecutionEnabled(@Nullable Boolean internalToolExecutionEnabled) {
			this.options.setInternalToolExecutionEnabled(internalToolExecutionEnabled);
			return this;
		}

		public Builder httpHeaders(Map<String, String> httpHeaders) {
			this.options.httpHeaders = httpHeaders;
			return this;
		}

		public Builder toolContext(Map<String, Object> toolContext) {
			if (this.options.toolContext == null) {
				this.options.toolContext = toolContext;
			}
			else {
				this.options.toolContext.putAll(toolContext);
			}
			return this;
		}

		public HunYuanChatOptions build() {
			return this.options;
		}

	}

}
