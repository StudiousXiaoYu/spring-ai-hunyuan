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

package io.github.studiousxiaoyu.hunyuan.audio;

import io.github.studiousxiaoyu.hunyuan.HunYuanTestConfiguration;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanAudioApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = HunYuanTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
class HunYuanAudioTranscriptionModelIT {

	@Value("classpath:/speech/speech1.mp3")
	private Resource audioFile;

	@Autowired
	protected HunYuanAudioTranscriptionModel transcriptionModel;

	@Test
	void transcriptionTest() {
		HunYuanAudioTranscriptionModelOptions transcriptionOptions = HunYuanAudioTranscriptionModelOptions.builder()
				.withEngSerViceType(HunYuanAudioApi.TranscriptionModel.EIGHT_K_EN.getValue())
				.withVoiceFormat("mp3")
			.build();
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile,
				transcriptionOptions);
		AudioTranscriptionResponse response = this.transcriptionModel.call(transcriptionRequest);
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().toLowerCase().contains("fellow")).isTrue();
	}

	@Test
	void transcriptionTestWithOptions() {
		TranscriptResponseFormat responseFormat = TranscriptResponseFormat.VTT;

		OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
			.language("en")
			.prompt("Ask not this, but ask that")
			.temperature(0f)
			.responseFormat(responseFormat)
			.build();
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile,
				transcriptionOptions);
		AudioTranscriptionResponse response = this.transcriptionModel.call(transcriptionRequest);
		assertThat(response.getResults()).hasSize(1);
		assertThat(response.getResults().get(0).getOutput().toLowerCase().contains("fellow")).isTrue();
	}

}
