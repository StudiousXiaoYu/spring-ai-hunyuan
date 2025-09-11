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

import io.github.studiousxiaoyu.hunyuan.api.HunYuanAudioApi;
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTranscriptionModelOptions;
import org.springframework.ai.document.MetadataMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HunYuanAudioTranscriptionProperties.CONFIG_PREFIX)
public class HunYuanAudioTranscriptionProperties extends HunYuanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.hunyuan.audio.transcription";

	public static final String DEFAULT_AUDIO_MODEL = HunYuanAudioApi.TranscriptionModel.SIXTEEN_K_ZH_PY.getValue();

	public static final String DEFAULT_AUDIO_FORMAT = "mp3";


	@NestedConfigurationProperty
	private HunYuanAudioTranscriptionModelOptions options = HunYuanAudioTranscriptionModelOptions.builder().withModel(DEFAULT_AUDIO_MODEL).withVoiceFormat(DEFAULT_AUDIO_FORMAT).build();

	public HunYuanAudioTranscriptionModelOptions getOptions() {
		return this.options;
	}

	public void setOptions(HunYuanAudioTranscriptionModelOptions options) {
		this.options = options;
	}

}
