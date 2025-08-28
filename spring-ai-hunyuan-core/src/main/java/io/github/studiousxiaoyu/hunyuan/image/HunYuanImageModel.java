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

package io.github.studiousxiaoyu.hunyuan.image;

import io.github.studiousxiaoyu.hunyuan.api.HunYuanConstants;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.*;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationContext;
import org.springframework.ai.image.observation.ImageModelObservationConvention;
import org.springframework.ai.image.observation.ImageModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.*;

/**
 * HunYuanImageModel is a {@link ImageModel} implementation that uses the HunYuan
 *
 * @author Guo Junyu
 */
public class HunYuanImageModel implements ImageModel {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanImageModel.class);

	private static final ImageModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultImageModelObservationConvention();

	private final RetryTemplate retryTemplate;

	private final HunYuanImageOptions defaultOptions;

	private final HunYuanImageApi hunYuanImageApi;

	/**
	 * Observation registry used for instrumentation.
	 */
	private final ObservationRegistry observationRegistry;

	/**
	 * Conventions to use for generating observations.
	 */
	private ImageModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;

	public HunYuanImageModel(HunYuanImageApi hunYuanImageApi) {
		this(hunYuanImageApi, HunYuanImageOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	/**
	 * Initializes a new instance of the HunYuanImageModel.
	 * @param hunYuanImageApi The HunYuanImageApi instance to be used for interacting with
	 * the HunYuan Image API.
	 * @param options The HunYuanImageOptions to configure the image model.
	 * @param retryTemplate The retry template.
	 */
	public HunYuanImageModel(HunYuanImageApi hunYuanImageApi, HunYuanImageOptions options, RetryTemplate retryTemplate) {
		this(hunYuanImageApi, options, retryTemplate, ObservationRegistry.NOOP);
	}

	/**
	 * Initializes a new instance of the HunYuanImageModel.
	 * @param hunYuanImageApi The HunYuanImageApi instance to be used for interacting with
	 * the HunYuan Image API.
	 * @param options The HunYuanImageOptions to configure the image model.
	 * @param retryTemplate The retry template.
	 * @param observationRegistry The ObservationRegistry used for instrumentation.
	 */
	public HunYuanImageModel(HunYuanImageApi hunYuanImageApi, HunYuanImageOptions options, RetryTemplate retryTemplate,
							ObservationRegistry observationRegistry) {
		Assert.notNull(hunYuanImageApi, "HunYuanImageApi must not be null");
		Assert.notNull(options, "options must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		Assert.notNull(observationRegistry, "observationRegistry must not be null");
		this.hunYuanImageApi = hunYuanImageApi;
		this.defaultOptions = options;
		this.retryTemplate = retryTemplate;
		this.observationRegistry = observationRegistry;
	}


	@Override
	public ImageResponse call(ImagePrompt request) {
		// Before moving any further, build the final request ImagePrompt,
		// merging runtime and default options.
		ImagePrompt requestImagePrompt = buildRequestImagePrompt(request);

		// 获取任务ID
		HunYuanImageApi.HunYuanImageRequest imageRequest = submitImageGenTask(requestImagePrompt);

		var observationContext = ImageModelObservationContext.builder()
				.imagePrompt(imagePrompt)
				.provider(HunYuanConstants.PROVIDER_NAME)
				.build();

		Observation observation = ImageModelObservationDocumentation.IMAGE_MODEL_OPERATION
				.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
						this.observationRegistry);

		return observation
				.observe(() -> {
					ResponseEntity<HunYuanImageApi.HunYuanImageResponse> imageResponseEntity = this.retryTemplate
							.execute(ctx -> {
								observation.lowCardinalityKeyValue("retry.attempt", String.valueOf(ctx.getRetryCount()));

								HunYuanImageApi.HunYuanImageAsyncResponse resp = getImageGenTask(taskId);
								if (resp != null) {
									String status = resp.output().taskStatus();
									observation.lowCardinalityKeyValue("task.status", status);

									switch (status) {
										case HunYuanConstants.STATUS_SUCCESS -> {
											return toImageResponse(resp);
										}
										case HunYuanConstants.STATUS_FAILED -> {
											return new ImageResponse(List.of(), toMetadata(resp));
										}
									}
								}
								throw new RuntimeException(HunYuanConstants.IMAGE_RUNNING_MESSAGE);
							}, context -> {
								observation.lowCardinalityKeyValue("timeout", "true");
								return new ImageResponse(List.of(), toMetadataTimeout(taskId));
							});
				});
	}

	private HunYuanImageApi.HunYuanImageRequest createRequest(ImagePrompt imagePrompt) {
		String instructions = imagePrompt.getInstructions().get(0).getText();
		HunYuanImageOptions imageOptions = (HunYuanImageOptions) imagePrompt.getOptions();

		HunYuanImageApi.HunYuanImageRequest imageRequest = new HunYuanImageApi.HunYuanImageRequest(instructions,
				HunYuanImageApi.DEFAULT_IMAGE_MODEL);

		return ModelOptionsUtils.merge(imageOptions, imageRequest, HunYuanImageApi.HunYuanImageRequest.class);
	}

	private ImageResponse convertResponse(ResponseEntity<HunYuanImageApi.HunYuanImageResponse> imageResponseEntity,
										  HunYuanImageApi.HunYuanImageRequest HunYuanImageRequest) {
		HunYuanImageApi.HunYuanImageResponse imageApiResponse = imageResponseEntity.getBody();
		if (imageApiResponse == null) {
			logger.warn("No image response returned for request: {}", HunYuanImageRequest);
			return new ImageResponse(List.of());
		}

		List<ImageGeneration> imageGenerationList = imageApiResponse.data()
				.stream()
				.map(entry -> new ImageGeneration(new Image(entry.url(), entry.b64Json()),
						new HunYuanImageGenerationMetadata(entry.revisedPrompt())))
				.toList();

		ImageResponseMetadata HunYuanImageResponseMetadata = new ImageResponseMetadata(imageApiResponse.created());
		return new ImageResponse(imageGenerationList, HunYuanImageResponseMetadata);
	}

	private ImagePrompt buildRequestImagePrompt(ImagePrompt imagePrompt) {
		// Process runtime options
		HunYuanImageOptions runtimeOptions = null;
		if (imagePrompt.getOptions() != null) {
			runtimeOptions = ModelOptionsUtils.copyToTarget(imagePrompt.getOptions(), ImageOptions.class,
					HunYuanImageOptions.class);
		}

		HunYuanImageOptions requestOptions = runtimeOptions == null ? this.defaultOptions : HunYuanImageOptions.builder()
				// Handle portable image options
				.model(ModelOptionsUtils.mergeOption(runtimeOptions.getModel(), this.defaultOptions.getModel()))
				.N(ModelOptionsUtils.mergeOption(runtimeOptions.getN(), this.defaultOptions.getN()))
				.responseFormat(ModelOptionsUtils.mergeOption(runtimeOptions.getResponseFormat(),
						this.defaultOptions.getResponseFormat()))
				.width(ModelOptionsUtils.mergeOption(runtimeOptions.getWidth(), this.defaultOptions.getWidth()))
				.height(ModelOptionsUtils.mergeOption(runtimeOptions.getHeight(), this.defaultOptions.getHeight()))
				.style(ModelOptionsUtils.mergeOption(runtimeOptions.getStyle(), this.defaultOptions.getStyle()))
				// Handle HunYuan specific image options
				.quality(ModelOptionsUtils.mergeOption(runtimeOptions.getQuality(), this.defaultOptions.getQuality()))
				.user(ModelOptionsUtils.mergeOption(runtimeOptions.getUser(), this.defaultOptions.getUser()))
				.build();

		return new ImagePrompt(imagePrompt.getInstructions(), requestOptions);
	}

	/**
	 * Use the provided convention for reporting observation data
	 * @param observationConvention The provided convention
	 */
	public void setObservationConvention(ImageModelObservationConvention observationConvention) {
		Assert.notNull(observationConvention, "observationConvention cannot be null");
		this.observationConvention = observationConvention;
	}
}
