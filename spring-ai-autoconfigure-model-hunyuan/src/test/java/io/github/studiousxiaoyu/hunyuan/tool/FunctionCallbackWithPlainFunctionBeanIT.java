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

package io.github.studiousxiaoyu.hunyuan.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.studiousxiaoyu.hunyuan.HunYuanAutoConfiguration;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import io.github.studiousxiaoyu.hunyuan.HunYuanChatModel;
import io.github.studiousxiaoyu.hunyuan.HunYuanChatOptions;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guo Junyu
 */
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
class FunctionCallbackWithPlainFunctionBeanIT {

	private final Logger logger = LoggerFactory.getLogger(FunctionCallbackWithPlainFunctionBeanIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.hunyuan.secret-id=" + System.getenv("HUNYUAN_SECRET_ID"))
		.withPropertyValues("spring.ai.hunyuan.secret-key=" + System.getenv("HUNYUAN_SECRET_KEY"))
		.withConfiguration(AutoConfigurations.of(HunYuanAutoConfiguration.class))
		.withUserConfiguration(Config.class);

	@Test
	void functionCallTest() {
		this.contextRunner.run(context -> {

			HunYuanChatModel chatModel = context.getBean(HunYuanChatModel.class);

			// Test weatherFunction
			UserMessage userMessage = new UserMessage(
					"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius");

			ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
					HunYuanChatOptions.builder().toolNames("weatherFunction").build()));

			logger.info("Response: {}", response);

			assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");

			// Test weatherFunctionTwo
			response = chatModel.call(new Prompt(List.of(userMessage),
					HunYuanChatOptions.builder().toolNames("weatherFunctionTwo").build()));

			logger.info("Response: {}", response);

			assertThat(response.getResult().getOutput().getText()).contains("30", "10", "15");

		});
	}

	@Test
	void functionCallWithPortableFunctionCallingOptions() {
		this.contextRunner.run(context -> {

			HunYuanChatModel chatModel = context.getBean(HunYuanChatModel.class);

			// Test weatherFunction
			UserMessage userMessage = new UserMessage(
					"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius");

			ChatResponse response = chatModel.call(new Prompt(List.of(userMessage),
					HunYuanChatOptions.builder().toolNames("weatherFunction").build()));

			logger.info("Response: {}", response);
		});
	}

	@Test
	void streamFunctionCallTest() {
		this.contextRunner.run(context -> {

			HunYuanChatModel chatModel = context.getBean(HunYuanChatModel.class);

			// Test weatherFunction
			UserMessage userMessage = new UserMessage(
					"What's the weather like in San Francisco, Tokyo, and Paris? Return the temperature in Celsius");

			Flux<ChatResponse> response = chatModel.stream(new Prompt(List.of(userMessage),
					HunYuanChatOptions.builder().toolNames("weatherFunction").build()));

			String content = response.collectList()
				.block()
				.stream()
				.map(ChatResponse::getResults)
				.flatMap(List::stream)
				.map(Generation::getOutput)
				.map(AssistantMessage::getText)
				.collect(Collectors.joining());
			logger.info("Response: {}", content);

			assertThat(content).containsAnyOf("30.0", "30");
			assertThat(content).containsAnyOf("10.0", "10");
			assertThat(content).containsAnyOf("15.0", "15");

			// Test weatherFunctionTwo
			response = chatModel.stream(new Prompt(List.of(userMessage),
					HunYuanChatOptions.builder().toolNames("weatherFunctionTwo").build()));

			content = response.collectList()
				.block()
				.stream()
				.map(ChatResponse::getResults)
				.flatMap(List::stream)
				.map(Generation::getOutput)
				.map(AssistantMessage::getText)
				.collect(Collectors.joining());
			logger.info("Response: {}", content);

			assertThat(content).containsAnyOf("30.0", "30");
			assertThat(content).containsAnyOf("10.0", "10");
			assertThat(content).containsAnyOf("15.0", "15");
		});
	}

	@Configuration
	static class Config {

		@Bean
		@Description("Get the weather in location")
		public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunction() {
			return new MockWeatherService();
		}

		// Relies on the Request's JsonClassDescription annotation to provide the
		// function description.
		@Bean
		public Function<MockWeatherService.Request, MockWeatherService.Response> weatherFunctionTwo() {
			MockWeatherService weatherService = new MockWeatherService();
			return (weatherService::apply);
		}

	}

}
