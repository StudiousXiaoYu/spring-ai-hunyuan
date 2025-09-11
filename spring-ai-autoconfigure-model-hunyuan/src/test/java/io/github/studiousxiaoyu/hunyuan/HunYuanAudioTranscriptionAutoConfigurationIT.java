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

import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanEmbeddingModel;
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTranscriptionModel;
import io.github.studiousxiaoyu.hunyuan.chat.HunYuanChatModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
public class HunYuanAudioTranscriptionAutoConfigurationIT {

	private static final Log logger = LogFactory.getLog(HunYuanAudioTranscriptionAutoConfigurationIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.hunyuan.secret-id=" + System.getenv("HUNYUAN_SECRET_ID"))
		.withPropertyValues("spring.ai.hunyuan.secret-key=" + System.getenv("HUNYUAN_SECRET_KEY"))
		.withConfiguration(AutoConfigurations.of(HunYuanAutoConfiguration.class));
	@Test
	void transcribe() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTranscriptionAutoConfiguration.class))
				.run(context -> {
					HunYuanAudioTranscriptionModel transcriptionModel = context.getBean(HunYuanAudioTranscriptionModel.class);
					Resource audioFile = new ClassPathResource("/speech/speech1.mp3");
					String response = transcriptionModel.call(audioFile);
					assertThat(response).isNotEmpty();
					logger.info("Response: " + response);
				});
	}
	@Test
	public void transcriptionOptionsTest() {

		new ApplicationContextRunner().withPropertyValues(
						// @formatter:off
						"spring.ai.hunyuan.secret-id=API_ID",
						"spring.ai.hunyuan.secret-key=API_KEY",
						"spring.ai.hunyuan.base-url=TEST_BASE_URL",
						"spring.ai.hunyuan.audio.transcription.options.model=MODEL_XYZ",
						"spring.ai.hunyuan.audio.transcription.options.voiceFormat=mp3"
				)
				// @formatter:on
				.withConfiguration(AutoConfigurations.of(HunYuanAudioTranscriptionAutoConfiguration.class))
				.run(context -> {
					var transcriptionProperties = context.getBean(HunYuanAudioTranscriptionProperties.class);
					var connectionProperties = context.getBean(HunYuanCommonProperties.class);

					assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
					assertThat(connectionProperties.getSecretKey()).isEqualTo("API_KEY");
					assertThat(connectionProperties.getSecretId()).isEqualTo("API_ID");
					assertThat(transcriptionProperties.getOptions().getModel()).isEqualTo("MODEL_XYZ");
					assertThat(transcriptionProperties.getOptions().getVoiceFormat()).isEqualTo("mp3");
				});
	}
	@Test
	void audioTranscriptionModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTranscriptionAutoConfiguration.class))
				.run(context -> {
					System.out.println(context.getBeanDefinitionNames());
					assertThat(context.getBeansOfType(HunYuanChatModel.class)).isEmpty();
					assertThat(context.getBeansOfType(HunYuanEmbeddingModel.class)).isEmpty();
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isNotEmpty();
				});

		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTranscriptionAutoConfiguration.class))
				.withPropertyValues("spring.ai.hunyuan.audio.transcription.enabled=false")
				.run(context -> {
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionProperties.class)).isEmpty();
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isEmpty();
				});

		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTranscriptionAutoConfiguration.class))
				.withPropertyValues("spring.ai.hunyuan.audio.transcription.enabled=true")
				.run(context -> {
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionProperties.class)).isNotEmpty();
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isNotEmpty();
				});

		this.contextRunner
				.withConfiguration(
						AutoConfigurations.of(HunYuanAutoConfiguration.class, HunYuanAudioTranscriptionAutoConfiguration.class))
				.withPropertyValues("spring.ai.hunyuan.chat.enabled=false",
						"spring.ai.hunyuan.embedding.enabled=false",
						"spring.ai.hunyuan.audio.transcription.enabled=false")
				.run(context -> {
					assertThat(context.getBeansOfType(HunYuanChatModel.class)).isEmpty();
					assertThat(context.getBeansOfType(HunYuanEmbeddingModel.class)).isEmpty();
					assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isNotEmpty();
				});
	}

}
