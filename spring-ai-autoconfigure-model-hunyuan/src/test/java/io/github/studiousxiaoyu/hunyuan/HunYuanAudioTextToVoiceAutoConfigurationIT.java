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
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTextToVoiceModel;
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTranscriptionModel;
import io.github.studiousxiaoyu.hunyuan.chat.HunYuanChatModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Guo Junyu
 */
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
public class HunYuanAudioTextToVoiceAutoConfigurationIT {

	private static final Log logger = LogFactory.getLog(HunYuanAudioTextToVoiceAutoConfigurationIT.class);

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withPropertyValues("spring.ai.hunyuan.secret-id=" + System.getenv("HUNYUAN_SECRET_ID"))
		.withPropertyValues("spring.ai.hunyuan.secret-key=" + System.getenv("HUNYUAN_SECRET_KEY"))
		.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class));

	@Test
	void transcribe() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class))
			.run(context -> {
				HunYuanAudioTextToVoiceModel transcriptionModel = context.getBean(HunYuanAudioTextToVoiceModel.class);

				String audioText = "你好";
				byte[] response = transcriptionModel.call(audioText);
				assertThat(response).isNotEmpty();
			});
	}

	@Test
	public void transcriptionOptionsTest() {

		new ApplicationContextRunner().withPropertyValues(
		// @formatter:off
						"spring.ai.hunyuan.secret-id=API_ID",
						"spring.ai.hunyuan.secret-key=API_KEY",
						"spring.ai.hunyuan.base-url=TEST_BASE_URL",
						"spring.ai.hunyuan.audio.tts.options.voiceType=101011"
				)
				// @formatter:on
			.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class))
			.run(context -> {
				var transcriptionProperties = context.getBean(HunYuanAudioTextToVoiceProperties.class);
				var connectionProperties = context.getBean(HunYuanCommonProperties.class);

				assertThat(connectionProperties.getBaseUrl()).isEqualTo("TEST_BASE_URL");
				assertThat(connectionProperties.getSecretKey()).isEqualTo("API_KEY");
				assertThat(connectionProperties.getSecretId()).isEqualTo("API_ID");
				assertThat(transcriptionProperties.getOptions().getVoiceType()).isEqualTo(101011);
			});
	}

	@Test
	void audioTranscriptionModelActivation() {
		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class))
			.run(context -> {
				assertThat(context.getBeansOfType(HunYuanChatModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanEmbeddingModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceModel.class)).isNotEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class))
			.withPropertyValues("spring.ai.hunyuan.audio.tts.enabled=false")
			.run(context -> {
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceModel.class)).isEmpty();
			});

		this.contextRunner.withConfiguration(AutoConfigurations.of(HunYuanAudioTextToVoiceAutoConfiguration.class))
			.withPropertyValues("spring.ai.hunyuan.audio.tts.enabled=true")
			.run(context -> {
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceProperties.class)).isNotEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceModel.class)).isNotEmpty();
			});

		this.contextRunner
			.withConfiguration(AutoConfigurations.of(HunYuanAutoConfiguration.class,
					HunYuanAudioTextToVoiceAutoConfiguration.class))
			.withPropertyValues("spring.ai.hunyuan.chat.enabled=false", "spring.ai.hunyuan.embedding.enabled=false",
					"spring.ai.hunyuan.audio.tts.enabled=false", "spring.ai.hunyuan.audio.transcription.enabled=false")
			.run(context -> {
				assertThat(context.getBeansOfType(HunYuanChatModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanEmbeddingModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTranscriptionModel.class)).isEmpty();
				assertThat(context.getBeansOfType(HunYuanAudioTextToVoiceModel.class)).isEmpty();
			});
	}

}
