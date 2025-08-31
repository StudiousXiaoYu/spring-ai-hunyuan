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

import io.github.studiousxiaoyu.hunyuan.HunYuanTestConfiguration;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import io.github.studiousxiaoyu.hunyuan.chat.message.HunYuanAssistantMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guo Junyu
 */
@SpringBootTest(classes = HunYuanTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
public class HunYuanChatModelThinkIT {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanChatModelThinkIT.class);

	@Autowired
	protected ChatModel chatModel;

	@Autowired
	protected StreamingChatModel streamingChatModel;

	@Value("classpath:/prompts/system-message.st")
	private Resource systemResource;

	@Test
	void thinkingTest() {
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemResource);
		Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "Bob", "voice", "pirate"));
		UserMessage userMessage = new UserMessage(
				"Tell me about 3 famous pirates from the Golden Age of Piracy and why they did.");
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage),
				HunYuanChatOptions.builder().model(HunYuanApi.ChatModel.HUNYUAN_A13B.getName()).build());
		ChatResponse response = this.chatModel.call(prompt);
		assertThat(((HunYuanAssistantMessage) response.getResult().getOutput()).getReasoningContent()).isNotNull();
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().getText()).contains("Blackbeard");
	}

	@Test
	void thinkingStreamTest() {
		SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.systemResource);
		Message systemMessage = systemPromptTemplate.createMessage(Map.of("name", "Bob", "voice", "pirate"));
		UserMessage userMessage = new UserMessage(
				"Tell me about 3 famous pirates from the Golden Age of Piracy and why they did.");
		Prompt prompt = new Prompt(List.of(userMessage, systemMessage),
				HunYuanChatOptions.builder().model(HunYuanApi.ChatModel.HUNYUAN_A13B.getName()).build());
		String generationTextFromStream = this.streamingChatModel.stream(prompt)
				.collectList()
				.block()
				.stream()
				.map(ChatResponse::getResults)
				.flatMap(List::stream)
				.map(Generation::getOutput)
				.map(message -> {
					if (message instanceof HunYuanAssistantMessage) {
						HunYuanAssistantMessage hunYuanMessage = (HunYuanAssistantMessage) message;
						// 可以选择返回包含思考链和文本的对象，或者只返回需要的部分
						return "think: " + hunYuanMessage.getReasoningContent() +
								"/think " + hunYuanMessage.getText();
					}
					return message.getText();
				})
				.collect(Collectors.joining());
		logger.info("Response: {}", generationTextFromStream);
		assertThat(generationTextFromStream).isNotNull();
		assertThat(generationTextFromStream).contains("Blackbeard");
	}

	@Test
	void thinkingStructTest() {
		DefaultConversionService conversionService = new DefaultConversionService();
		ListOutputConverter outputConverter = new ListOutputConverter(conversionService);

		String format = outputConverter.getFormat();
		String template = """
				List five {subject}
				{format}
				""";
		PromptTemplate promptTemplate = PromptTemplate.builder()
			.template(template)
			.variables(Map.of("subject", "ice cream flavors", "format", format))
			.build();
		Prompt prompt = new Prompt(promptTemplate.createMessage(),
				HunYuanChatOptions.builder().model(HunYuanApi.ChatModel.HUNYUAN_A13B.getName()).build());
		Generation generation = this.chatModel.call(prompt).getResult();
		List<String> list = outputConverter.convert(generation.getOutput().getText());
		assertThat(((HunYuanAssistantMessage) generation.getOutput()).getReasoningContent()).isNotNull();
		logger.info("getReasoningContent:" + ((HunYuanAssistantMessage) generation.getOutput()).getReasoningContent());
		assertThat(list).hasSize(5);
	}

}
