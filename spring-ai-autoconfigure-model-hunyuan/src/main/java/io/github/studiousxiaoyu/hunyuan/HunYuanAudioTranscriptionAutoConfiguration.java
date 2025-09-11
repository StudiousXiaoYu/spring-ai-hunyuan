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
import io.github.studiousxiaoyu.hunyuan.audio.HunYuanAudioTranscriptionModel;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link AutoConfiguration Auto-configuration} for HunYuan Chat Model.
 *
 * @author Guo Junyu
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, WebClientAutoConfiguration.class,
		SpringAiRetryAutoConfiguration.class})
@EnableConfigurationProperties({ HunYuanCommonProperties.class,HunYuanAudioTranscriptionProperties.class  })
@ConditionalOnClass(HunYuanAudioApi.class)
@ImportAutoConfiguration(classes = { SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class,
		WebClientAutoConfiguration.class })
public class HunYuanAudioTranscriptionAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = HunYuanAudioTranscriptionProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public HunYuanAudioTranscriptionModel hunYuanAudioTranscriptionModel(HunYuanCommonProperties commonProperties,
			HunYuanAudioTranscriptionProperties chatProperties) {

		var hunyuanApi = hunYuanAudioApi(chatProperties.getSecretId(), commonProperties.getSecretId(),
				chatProperties.getSecretKey(), commonProperties.getSecretKey(), chatProperties.getBaseUrl(),
				commonProperties.getBaseUrl());

		return new HunYuanAudioTranscriptionModel(hunyuanApi);
	}

	private HunYuanAudioApi hunYuanAudioApi(String secretId, String commonSecretId, String secretKey, String commonSecretKey,
								  String baseUrl, String commonBaseUrl) {

		var resolvedSecretId = StringUtils.hasText(secretId) ? secretId : commonSecretId;
		var resolvedSecretKey = StringUtils.hasText(secretKey) ? secretKey : commonSecretKey;
		var resoledBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl : commonBaseUrl;

		Assert.hasText(resolvedSecretId, "HunYuan SecretId must be set");
		Assert.hasText(resolvedSecretKey, "HunYuan SecretKey must be set");
		Assert.hasText(resoledBaseUrl, "HunYuan base URL must be set");

		return new HunYuanAudioApi(resoledBaseUrl, resolvedSecretId, resolvedSecretKey);
	}

}
