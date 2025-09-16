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
import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTextToVoiceModelOptions;
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTranscriptionModelOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HunYuanAudioTextToVoiceProperties.CONFIG_PREFIX)
public class HunYuanAudioTextToVoiceProperties extends HunYuanParentProperties {

	public static final String CONFIG_PREFIX = "spring.ai.hunyuan.audio.tts";

	public static final Integer DEFAULT_AUDIO_VOICE_TYPE = 101010;

	public static final String DEFAULT_AUDIO_BASE_URL = HunYuanConstants.DEFAULT_TTS_URL;

	public HunYuanAudioTextToVoiceProperties() {
		super.setBaseUrl(DEFAULT_AUDIO_BASE_URL);
	}

	@NestedConfigurationProperty
	private HunYuanAudioTextToVoiceModelOptions options = HunYuanAudioTextToVoiceModelOptions.builder()
		.voiceType(DEFAULT_AUDIO_VOICE_TYPE)
		.build();

	public HunYuanAudioTextToVoiceModelOptions getOptions() {
		return this.options;
	}

	public void setOptions(HunYuanAudioTextToVoiceModelOptions options) {
		this.options = options;
	}

}
