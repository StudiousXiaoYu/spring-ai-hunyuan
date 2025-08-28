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
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.retry.autoconfigure.SpringAiRetryAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * {@link AutoConfiguration Auto-configuration} for HunYuan Chat Model.
 *
 * @author Guo Junyu
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, WebClientAutoConfiguration.class,
		SpringAiRetryAutoConfiguration.class, ToolCallingAutoConfiguration.class })
@EnableConfigurationProperties({ HunYuanCommonProperties.class, HunYuanChatProperties.class,
		HunYuanEmbeddingProperties.class })
@ConditionalOnClass(HunYuanApi.class)
@ImportAutoConfiguration(classes = { SpringAiRetryAutoConfiguration.class, RestClientAutoConfiguration.class,
		WebClientAutoConfiguration.class, ToolCallingAutoConfiguration.class })
public class HunYuanAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = HunYuanChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public HunYuanChatModel hunyuanChatModel(HunYuanCommonProperties commonProperties,
                                             HunYuanChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                             ToolCallingManager toolCallingManager, RetryTemplate retryTemplate,
                                             ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry,
                                             ObjectProvider<ChatModelObservationConvention> observationConvention) {

		var hunyuanApi = hunyuanApi(chatProperties.getSecretId(), commonProperties.getSecretId(),
				chatProperties.getSecretKey(), commonProperties.getSecretKey(), chatProperties.getBaseUrl(),
				commonProperties.getBaseUrl(), restClientBuilderProvider.getIfAvailable(RestClient::builder),
				responseErrorHandler);

		var chatModel = new HunYuanChatModel(hunyuanApi, chatProperties.getOptions(), toolCallingManager, retryTemplate,
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		observationConvention.ifAvailable(chatModel::setObservationConvention);
		return chatModel;
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = HunYuanEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true",
			matchIfMissing = true)
	public HunYuanEmbeddingModel hunYuanEmbeddingModel(HunYuanCommonProperties commonProperties,
			HunYuanEmbeddingProperties embeddingProperties,
			ObjectProvider<RestClient.Builder> restClientBuilderProvider,
			ObjectProvider<WebClient.Builder> webClientBuilderProvider, RetryTemplate retryTemplate,
			ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {

		var hunyuanApi = hunyuanApi(embeddingProperties.getSecretId(), commonProperties.getSecretId(),
				embeddingProperties.getSecretKey(), commonProperties.getSecretKey(), embeddingProperties.getBaseUrl(),
				commonProperties.getBaseUrl(), restClientBuilderProvider.getIfAvailable(RestClient::builder),
				responseErrorHandler);

		var embeddingModel = new HunYuanEmbeddingModel(embeddingProperties.getOptions(), retryTemplate, hunyuanApi,
				embeddingProperties.getMetadataMode(), observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;
	}

	private HunYuanApi hunyuanApi(String secretId, String commonSecretId, String secretKey, String commonSecretKey,
			String baseUrl, String commonBaseUrl, RestClient.Builder restClientBuilder,
			ResponseErrorHandler responseErrorHandler) {

		var resolvedSecretId = StringUtils.hasText(secretId) ? secretId : commonSecretId;
		var resolvedSecretKey = StringUtils.hasText(secretKey) ? secretKey : commonSecretKey;
		var resoledBaseUrl = StringUtils.hasText(baseUrl) ? baseUrl : commonBaseUrl;

		Assert.hasText(resolvedSecretId, "HunYuan SecretId must be set");
		Assert.hasText(resolvedSecretKey, "HunYuan SecretKey must be set");
		Assert.hasText(resoledBaseUrl, "HunYuan base URL must be set");

		return new HunYuanApi(resoledBaseUrl, resolvedSecretId, resolvedSecretKey, restClientBuilder,
				responseErrorHandler);
	}

}
