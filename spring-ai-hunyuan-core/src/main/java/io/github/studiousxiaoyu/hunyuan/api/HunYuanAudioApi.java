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

package io.github.studiousxiaoyu.hunyuan.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.studiousxiaoyu.hunyuan.api.auth.ApiAuthHttpRequestInterceptor;
import io.github.studiousxiaoyu.hunyuan.api.auth.HunYuanAuthApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.ChatModelDescription;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 *
 * @author Guo Junyu
 */
public class HunYuanAudioApi {

	private static final Logger logger = LoggerFactory.getLogger(HunYuanAudioApi.class);

	private final RestClient restClient;

	private final ApiAuthHttpRequestInterceptor apiAuthHttpRequestInterceptor;

	/**
	 * Create a new client api with DEFAULT_TRANSCRIPTION_URL
	 * @param secretId Hunyuan SecretId.
	 * @param secretKey Hunyuan SecretKey.
	 */
	public HunYuanAudioApi(String secretId, String secretKey) {
		this(HunYuanConstants.DEFAULT_TRANSCRIPTION_URL, secretId, secretKey);
	}

	/**
	 * Create a new client api.
	 * @param baseUrl api base URL.
	 * @param secretId Hunyuan SecretId.
	 * @param secretKey Hunyuan SecretKey.
	 */
	public HunYuanAudioApi(String baseUrl, String secretId, String secretKey) {
		this(baseUrl, secretId, secretKey, HunYuanConstants.DEFAULT_TRANSCRIPTION_ACTION, RestClient.builder(),
				RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
	}
	/**
	 * Create a new client api.
	 * @param baseUrl api base URL.
	 * @param secretKey Hunyuan api Key.
	 * @param restClientBuilder RestClient builder.
	 * @param responseErrorHandler Response error handler.
	 */
	public HunYuanAudioApi(String baseUrl, String secretId, String secretKey, String action,
                           RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {

		this(baseUrl, secretId, secretKey, action,HunYuanConstants.DEFAULT_TRANSCRIPTION_SERVICE,HunYuanConstants.DEFAULT_TRANSCRIPTION_HOST,
				HunYuanConstants.DEFAULT_TRANSCRIPTION_VERSION,restClientBuilder,
				responseErrorHandler);
	}
	public HunYuanAudioApi(String baseUrl, String secretId, String secretKey, String action,String service,String host,String version,
						   RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
		Consumer<HttpHeaders> jsonContentHeaders = headers -> {
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.add("X-TC-Action", action);
			headers.add("X-TC-Version", version);
		};
		apiAuthHttpRequestInterceptor = new ApiAuthHttpRequestInterceptor(secretId, secretKey,host,service);
		this.restClient = restClientBuilder.baseUrl(baseUrl)
				.defaultHeaders(jsonContentHeaders)
				.defaultStatusHandler(responseErrorHandler)
				.requestInterceptor(apiAuthHttpRequestInterceptor)
				.build();

	}


	public ResponseEntity<TranscriptionResponse> createTranscription(TranscriptionRequest request, Class<TranscriptionResponse> transcriptionResponseClass) {

		ResponseEntity<String> response = this.restClient.post()
				.uri("/")
				.body(request)
				.retrieve()
				.toEntity(String.class);
		TranscriptionResponse transcriptionResponse = ModelOptionsUtils.jsonToObject(response.getBody(), transcriptionResponseClass);
		return ResponseEntity.ok(transcriptionResponse);
	}

	public ResponseEntity<TextToVoiceResponse> createTextToVoice(TextToVoiceRequest textToVoiceRequest, Class<TextToVoiceResponse> audioTextToVoiceResponseClass) {
		ResponseEntity<String> response = this.restClient.post()
				.uri("/")
				.body(textToVoiceRequest)
				.retrieve()
				.toEntity(String.class);
		TextToVoiceResponse textToVoiceResponse = ModelOptionsUtils.jsonToObject(response.getBody(), audioTextToVoiceResponseClass);
		return ResponseEntity.ok(textToVoiceResponse);
	}


	@JsonInclude(Include.NON_NULL)
	public record TranscriptionRequest(@JsonProperty("EngSerViceType") String engSerViceType,
									   @JsonProperty("SourceType") Integer sourceType,
									   @JsonProperty("VoiceFormat") String voiceFormat,
									   @JsonProperty("Url") String url,
									   @JsonProperty("Data") String data,
									   @JsonProperty("DataLen") Integer dataLen,
									   @JsonProperty("WordInfo") Integer wordInfo,
									   @JsonProperty("FilterDirty") Integer filterDirty,
									   @JsonProperty("FilterModal") Integer filterModal,
									   @JsonProperty("FilterPunc") Integer filterPunc,
									   @JsonProperty("ConvertNumMode") Integer convertNumMode,
									   @JsonProperty("HotwordId") String hotwordId,
									   @JsonProperty("CustomizationId") String customizationId,
									   @JsonProperty("HotwordList") String hotwordList,
									   @JsonProperty("InputSampleRate") String inputSampleRate){
		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			private String engSerViceType;
			private Integer sourceType;
			private String voiceFormat;
			private String url;
			private String data;
			private Integer dataLen;
			private Integer wordInfo;
			private Integer filterDirty;
			private Integer filterModal;
			private Integer filterPunc;
			private Integer convertNumMode;
			private String hotwordId;
			private String customizationId;
			private String hotwordList;
			private String inputSampleRate;
			public Builder engSerViceType(String engSerViceType) {
				this.engSerViceType = engSerViceType;
				return this;
			}
			public Builder sourceType(Integer sourceType) {
				this.sourceType = sourceType;
				return this;
			}
			public Builder voiceFormat(String voiceFormat) {
				this.voiceFormat = voiceFormat;
				return this;
			}
			public Builder url(String url) {
				this.url = url;
				return this;
			}
			public Builder data(String data) {
				this.data = data;
				return this;
			}
			public Builder dataLen(Integer dataLen) {
				this.dataLen = dataLen;
				return this;
			}
			public Builder wordInfo(Integer wordInfo) {
				this.wordInfo = wordInfo;
				return this;
			}
			public Builder filterDirty(Integer filterDirty) {
				this.filterDirty = filterDirty;
				return this;
			}
			public Builder filterModal(Integer filterModal) {
				this.filterModal = filterModal;
				return this;
			}
			public Builder filterPunc(Integer filterPunc) {
				this.filterPunc = filterPunc;
				return this;
			}
			public Builder convertNumMode(Integer convertNumMode) {
				this.convertNumMode = convertNumMode;
				return this;
			}
			public Builder hotwordId(String hotwordId) {
				this.hotwordId = hotwordId;
				return this;
			}
			public Builder customizationId(String customizationId) {
				this.customizationId = customizationId;
				return this;
			}
			public Builder hotwordList(String hotwordList) {
				this.hotwordList = hotwordList;
				return this;
			}
			public Builder inputSampleRate(String inputSampleRate) {
				this.inputSampleRate = inputSampleRate;
				return this;
			}
			public TranscriptionRequest build() {
				return new TranscriptionRequest(engSerViceType, sourceType, voiceFormat, url, data, dataLen, wordInfo,
						filterDirty, filterModal, filterPunc, convertNumMode, hotwordId, customizationId, hotwordList,
						inputSampleRate);
			}
		}
	}
	public enum TranscriptionModel {

		// @formatter:off
		EIGHT_K_ZH("8k_zh"),
		EIGHT_K_EN("8k_en"),
		SIXTEEN_K_ZH("16k_zh"),
		SIXTEEN_K_ZH_DIALECT("16k_zh_dialect"),
		SIXTEEN_K_ZH_PY("16k_zh-PY"),
		SIXTEEN_K_ZH_MEDICAL("16k_zh_medical"),
		SIXTEEN_K_EN("16k_en"),
		SIXTEEN_K_YUE("16k_yue"),
		SIXTEEN_K_JA("16k_ja"),
		SIXTEEN_K_KO("16k_ko"),
		SIXTEEN_K_VI("16k_vi"),
		SIXTEEN_K_MS("16k_ms"),
		SIXTEEN_K_ID("16k_id"),
		SIXTEEN_K_FIL("16k_fil"),
		SIXTEEN_K_TH("16k_th"),
		SIXTEEN_K_PT("16k_pt"),
		SIXTEEN_K_TR("16k_tr"),
		SIXTEEN_K_AR("16k_ar"),
		SIXTEEN_K_ES("16k_es"),
		SIXTEEN_K_HI("16k_hi"),
		SIXTEEN_K_FR("16k_fr"),
		SIXTEEN_K_DE("16k_de");
		// @formatter:on

		public final String value;

		TranscriptionModel(String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

	}
	@JsonInclude(Include.NON_NULL)
	public record TranscriptionResponse(
			// @formatter:off
			@JsonProperty("Response") TranscriptionDetail response
	) {
		// @formatter:on
	}
	@JsonInclude(Include.NON_NULL)
	public record TranscriptionDetail(@JsonProperty("Result") String result,
									   @JsonProperty("AudioDuration") Integer audioDuration,
									   @JsonProperty("WordSize") Integer wordSize,
									   @JsonProperty("WordList") List<SentenceWord> WordList,
									   @JsonProperty("RequestId") String requestId){
		public record SentenceWord(@JsonProperty("Word") String word,
								   @JsonProperty("StartTime") String startTime,
								   @JsonProperty("EndTime") String endTime){}
	}


	@JsonInclude(Include.NON_NULL)
	public record TextToVoiceRequest(
			@JsonProperty("Text") String text,
			@JsonProperty("SessionId") String sessionId,
			@JsonProperty("Volume") Float volume,
			@JsonProperty("Speed") Float speed,
			@JsonProperty("ProjectId") Integer projectId,
			@JsonProperty("ModelType") Integer modelType,
			@JsonProperty("VoiceType") Integer voiceType,
			@JsonProperty("FastVoiceType") String fastVoiceType,
			@JsonProperty("PrimaryLanguage") Integer primaryLanguage,
			@JsonProperty("SampleRate") Integer sampleRate,
			@JsonProperty("Codec") String codec,
			@JsonProperty("EnableSubtitle") Boolean enableSubtitle,
			@JsonProperty("SegmentRate") Integer segmentRate,
			@JsonProperty("EmotionCategory") String emotionCategory,
			@JsonProperty("EmotionIntensity") Integer emotionIntensity) {

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder {
			private String text;
			private String sessionId;
			private Float volume;
			private Float speed;
			private Integer projectId;
			private Integer modelType;
			private Integer voiceType;
			private String fastVoiceType;
			private Integer primaryLanguage;
			private Integer sampleRate;
			private String codec;
			private Boolean enableSubtitle;
			private Integer segmentRate;
			private String emotionCategory;
			private Integer emotionIntensity;

			public Builder text(String text) {
				this.text = text;
				return this;
			}

			public Builder sessionId(String sessionId) {
				this.sessionId = sessionId;
				return this;
			}

			public Builder volume(Float volume) {
				this.volume = volume;
				return this;
			}

			public Builder speed(Float speed) {
				this.speed = speed;
				return this;
			}

			public Builder projectId(Integer projectId) {
				this.projectId = projectId;
				return this;
			}

			public Builder modelType(Integer modelType) {
				this.modelType = modelType;
				return this;
			}

			public Builder voiceType(Integer voiceType) {
				this.voiceType = voiceType;
				return this;
			}

			public Builder fastVoiceType(String fastVoiceType) {
				this.fastVoiceType = fastVoiceType;
				return this;
			}

			public Builder primaryLanguage(Integer primaryLanguage) {
				this.primaryLanguage = primaryLanguage;
				return this;
			}

			public Builder sampleRate(Integer sampleRate) {
				this.sampleRate = sampleRate;
				return this;
			}

			public Builder codec(String codec) {
				this.codec = codec;
				return this;
			}

			public Builder enableSubtitle(Boolean enableSubtitle) {
				this.enableSubtitle = enableSubtitle;
				return this;
			}

			public Builder segmentRate(Integer segmentRate) {
				this.segmentRate = segmentRate;
				return this;
			}

			public Builder emotionCategory(String emotionCategory) {
				this.emotionCategory = emotionCategory;
				return this;
			}

			public Builder emotionIntensity(Integer emotionIntensity) {
				this.emotionIntensity = emotionIntensity;
				return this;
			}

			public TextToVoiceRequest build() {
				return new TextToVoiceRequest(text, sessionId, volume, speed, projectId, modelType,
						voiceType, fastVoiceType, primaryLanguage, sampleRate, codec, enableSubtitle,
						segmentRate, emotionCategory, emotionIntensity);
			}
		}

	}
	@JsonInclude(Include.NON_NULL)
	public record TextToVoiceResponse(
			// @formatter:off
			@JsonProperty("Response") HunYuanAudioApi.TextToVoiceDetail response
	) {
		// @formatter:on
	}
	@JsonInclude(Include.NON_NULL)
	public record TextToVoiceDetail(@JsonProperty("Audio") String audio,
									  @JsonProperty("SessionId") Integer sessionId,
									  @JsonProperty("Subtitles") List<Subtitle> subtitles,
									  @JsonProperty("RequestId") String requestId){
		public record Subtitle(@JsonProperty("Text") String text,
								   @JsonProperty("BeginTime") Integer beginTime,
								   @JsonProperty("EndTime") Integer endTime,
							   @JsonProperty("BeginIndex") Integer beginIndex,
							   @JsonProperty("EndIndex") Integer endIndex,
							   @JsonProperty("Phoneme") String phoneme
							   ){}
	}

}
