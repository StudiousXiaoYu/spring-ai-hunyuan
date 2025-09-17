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

import io.github.studiousxiaoyu.hunyuan.api.HunYuanEmbeddingModel;
import io.github.studiousxiaoyu.hunyuan.chat.HunYuanChatModel;
import io.github.studiousxiaoyu.hunyuan.chat.HunYuanChatOptions;
import io.github.studiousxiaoyu.hunyuan.chat.message.HunYuanAssistantMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guo Junyu
 */
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
public class HunYuanAutoConfigurationIT {

	private static final Log logger = LogFactory.getLog(HunYuanAutoConfigurationIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.hunyuan.secret-id=" + System.getenv("HUNYUAN_SECRET_ID"))
		.withPropertyValues("spring.ai.hunyuan.secret-key=" + System.getenv("HUNYUAN_SECRET_KEY"))
		.withConfiguration(AutoConfigurations.of(HunYuanAutoConfiguration.class));

	@Test
	void generate() {
		this.contextRunner.run(context -> {
			HunYuanChatModel client = context.getBean(HunYuanChatModel.class);
			String response = client.call("Hello");
			assertThat(response).isNotEmpty();
			logger.info("Response: " + response);
		});
	}

	@Test
	void generateThink() {
		this.contextRunner.run(context -> {
			HunYuanChatModel client = context.getBean(HunYuanChatModel.class);
			HunYuanChatOptions model = HunYuanChatOptions.builder().enableThinking(true).model("hunyuan-a13b").build();
			ChatResponse response = client.call(new Prompt(new UserMessage("hello"), model));
			assertThat(response.getResult().getOutput()).isNotNull();
			assertThat(((HunYuanAssistantMessage) response.getResult().getOutput()).getReasoningContent()).isNotNull();
		});
	}

	@Test
	void generateStreaming() {
		this.contextRunner.run(context -> {
			HunYuanChatModel client = context.getBean(HunYuanChatModel.class);
			Flux<ChatResponse> responseFlux = client.stream(new Prompt(new UserMessage("Hello")));
			String response = Objects.requireNonNull(responseFlux.collectList().block())
				.stream()
				.map(chatResponse -> chatResponse.getResults().get(0).getOutput().getText())
				.collect(Collectors.joining());

			assertThat(response).isNotEmpty();
			logger.info("Response: " + response);
		});
	}

	@Test
	void embedding() {
		this.contextRunner.run(context -> {
			HunYuanEmbeddingModel embeddingModel = context.getBean(HunYuanEmbeddingModel.class);

			EmbeddingResponse embeddingResponse = embeddingModel
				.embedForResponse(List.of("Hello World", "World is big and salvation is near"));
			assertThat(embeddingResponse.getResults()).hasSize(2);
			assertThat(embeddingResponse.getResults().get(0).getOutput()).isNotEmpty();
			assertThat(embeddingResponse.getResults().get(0).getIndex()).isEqualTo(0);
			assertThat(embeddingResponse.getResults().get(1).getOutput()).isNotEmpty();
			assertThat(embeddingResponse.getResults().get(1).getIndex()).isEqualTo(1);

			assertThat(embeddingModel.dimensions()).isEqualTo(1024);
		});
	}

}
