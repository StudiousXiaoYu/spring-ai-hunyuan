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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.function.FunctionToolCallback;
import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import io.github.studiousxiaoyu.hunyuan.HunYuanChatOptions;
import io.github.studiousxiaoyu.hunyuan.HunYuanTestConfiguration;
import io.github.studiousxiaoyu.hunyuan.api.MockWeatherService;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guo Junyu
 */
@SpringBootTest(classes = HunYuanTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
class HunYuanChatModelFunctionCallingIT {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanChatModelFunctionCallingIT.class);

	@Autowired
	ChatModel chatModel;

	@Test
	void functionCallTest() {

		UserMessage userMessage = new UserMessage(
				"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius.");

		List<Message> messages = new ArrayList<>(List.of(userMessage));

		var promptOptions = HunYuanChatOptions.builder()
			.model(HunYuanApi.ChatModel.HUNYUAN_PRO.getValue())
			.toolCallbacks(List.of(FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
				.description("Get the weather in location")
				.inputType(MockWeatherService.Request.class)
				.build()))
			.build();

		ChatResponse response = this.chatModel.call(new Prompt(messages, promptOptions));

		logger.info("Response: {}", response);

		assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");
	}

	@Test
	void streamFunctionCallTest() {

		UserMessage userMessage = new UserMessage(
				"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius.");

		List<Message> messages = new ArrayList<>(List.of(userMessage));

		var promptOptions = HunYuanChatOptions.builder()
			.toolCallbacks(List.of(FunctionToolCallback.builder("getCurrentWeather", new MockWeatherService())
				.description("Get the weather in location")
				.inputType(MockWeatherService.Request.class)
				.build()))
			.build();

		Flux<ChatResponse> response = this.chatModel.stream(new Prompt(messages, promptOptions));

		String content = response.collectList()
			.block()
			.stream()
			.map(ChatResponse::getResults)
			.flatMap(List::stream)
			.map(Generation::getOutput)
			.map(AssistantMessage::getText)
			.filter(Objects::nonNull)
			.collect(Collectors.joining());
		logger.info("Response: {}", content);

		assertThat(content).contains("30", "10", "15");
	}

}
