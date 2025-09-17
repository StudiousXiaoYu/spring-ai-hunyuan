package io.github.studiousxiaoyu.hunyuan.audio;

import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanAudioApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.metadata.RateLimit;
import org.springframework.ai.model.Model;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;

public class HunYuanAudioTranscriptionModel implements Model<AudioTranscriptionPrompt, AudioTranscriptionResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RetryTemplate retryTemplate;

	private final HunYuanAudioApi hunYuanAudioApi;

	private final HunYuanAudioTranscriptionModelOptions defaultOptions;

	public HunYuanAudioTranscriptionModel(HunYuanAudioApi hunYuanAudioApi) {
		this(hunYuanAudioApi,
				HunYuanAudioTranscriptionModelOptions.builder()
					.withEngSerViceType(HunYuanAudioApi.TranscriptionModel.SIXTEEN_K_ZH_PY.getValue())
					.withVoiceFormat("mp3")
					.build());
	}

	public HunYuanAudioTranscriptionModel(HunYuanAudioApi hunYuanAudioApi,
			HunYuanAudioTranscriptionModelOptions options) {
		this(RetryUtils.DEFAULT_RETRY_TEMPLATE, hunYuanAudioApi, options);
	}

	public HunYuanAudioTranscriptionModel(RetryTemplate retryTemplate, HunYuanAudioApi hunYuanAudioApi,
			HunYuanAudioTranscriptionModelOptions options) {
		this.retryTemplate = retryTemplate;
		this.hunYuanAudioApi = hunYuanAudioApi;
		this.defaultOptions = options;
	}

	public String call(Resource audioResource) {
		AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioResource);
		return call(transcriptionRequest).getResult().getOutput();
	}

	@Override
	public AudioTranscriptionResponse call(AudioTranscriptionPrompt transcriptionPrompt) {
		Resource audioResource = transcriptionPrompt.getInstructions();
		HunYuanAudioApi.TranscriptionRequest request = createRequest(transcriptionPrompt);
		ResponseEntity<HunYuanAudioApi.TranscriptionResponse> transcriptionEntity = this.retryTemplate.execute(
				ctx -> this.hunYuanAudioApi.createTranscription(request, HunYuanAudioApi.TranscriptionResponse.class));

		var transcription = transcriptionEntity.getBody();

		if (transcription == null) {
			logger.warn("No transcription returned for request: {}", audioResource);
			return new AudioTranscriptionResponse(null);
		}

		if (transcription.response().errorMsg() != null) {
			String index = transcription.response().errorMsg().index();
			String message = transcription.response().errorMsg().message();
			throw new RuntimeException("Error in textToVoice request: " + index + ": " + message);
		}

		AudioTranscription transcript = new AudioTranscription(transcription.response().result());

		return new AudioTranscriptionResponse(transcript);
	}

	private HunYuanAudioApi.TranscriptionRequest createRequest(AudioTranscriptionPrompt transcriptionPrompt) {

		HunYuanAudioTranscriptionModelOptions options = this.defaultOptions;

		if (transcriptionPrompt.getOptions() != null) {
			if (transcriptionPrompt.getOptions() instanceof HunYuanAudioTranscriptionModelOptions runtimeOptions) {
				options = this.merge(runtimeOptions, options);
			}
			else {
				throw new IllegalArgumentException("Prompt options are not of type TranscriptionOptions: "
						+ transcriptionPrompt.getOptions().getClass().getSimpleName());
			}
		}

		Resource instructions = transcriptionPrompt.getInstructions();
		HunYuanAudioApi.TranscriptionRequest.Builder transcriptionRequestBuilder = HunYuanAudioApi.TranscriptionRequest
			.builder()
			.engSerViceType(options.getEngSerViceType())
			.voiceFormat(options.getVoiceFormat());
		try {
			if (instructions instanceof UrlResource) {
				URL url = instructions.getURL();
				transcriptionRequestBuilder.url(url.toString()).sourceType(0);
			}
			else {
				// 语音数据，当SourceType 值为1（本地语音数据上传）时必须填写，当SourceType 值为0（语音
				// URL上传）可不写。要使用base64编码(采用python语言时注意读取文件应该为string而不是byte，以byte格式读取后要decode()。编码后的数据不可带有回车换行符)。音频时长不能超过60s，音频文件大小不能超过3MB（Base64后）。
				String cfd = toBase64(instructions.getInputStream());
				transcriptionRequestBuilder.data(cfd).dataLen(cfd.length()).sourceType(1);
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		transcriptionRequestBuilder.convertNumMode(options.getConvertNumMode())
			.customizationId(options.getCustomizationId())
			.filterDirty(options.getFilterDirty())
			.filterModal(options.getFilterModal())
			.filterPunc(options.getFilterPunc())
			.hotwordId(options.getHotwordId())
			.hotwordList(options.getHotwordList())
			.inputSampleRate(options.getInputSampleRate())
			.wordInfo(options.getWordInfo());
		return transcriptionRequestBuilder.build();
	}

	private String toBase64(InputStream inputStream) {
		try {
			return Base64.getEncoder().encodeToString(inputStream.readAllBytes()).replaceAll("\r|\n", "");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HunYuanAudioTranscriptionModelOptions merge(HunYuanAudioTranscriptionModelOptions source,
			HunYuanAudioTranscriptionModelOptions defaultOptions) {
		if (source == null) {
			source = new HunYuanAudioTranscriptionModelOptions();
		}
		HunYuanAudioTranscriptionModelOptions merged = new HunYuanAudioTranscriptionModelOptions();
		merged.setEngSerViceType(source.getModel() != null ? source.getModel() : defaultOptions.getModel());
		merged.setEngSerViceType(
				source.getEngSerViceType() != null ? source.getEngSerViceType() : defaultOptions.getEngSerViceType());
		merged.setSourceType(source.getSourceType() != null ? source.getSourceType() : defaultOptions.getSourceType());
		merged.setCustomizationId(source.getCustomizationId() != null ? source.getCustomizationId()
				: defaultOptions.getCustomizationId());
		merged.setConvertNumMode(
				source.getConvertNumMode() != null ? source.getConvertNumMode() : defaultOptions.getConvertNumMode());
		merged.setFilterDirty(
				source.getFilterDirty() != null ? source.getFilterDirty() : defaultOptions.getFilterDirty());
		merged.setFilterPunc(source.getFilterPunc() != null ? source.getFilterPunc() : defaultOptions.getFilterPunc());
		merged.setFilterModal(
				source.getFilterModal() != null ? source.getFilterModal() : defaultOptions.getFilterModal());
		merged.setHotwordId(source.getHotwordId() != null ? source.getHotwordId() : defaultOptions.getHotwordId());
		merged.setHotwordList(
				source.getHotwordList() != null ? source.getHotwordList() : defaultOptions.getHotwordList());
		merged.setInputSampleRate(source.getInputSampleRate() != null ? source.getInputSampleRate()
				: defaultOptions.getInputSampleRate());
		merged.setWordInfo(source.getWordInfo() != null ? source.getWordInfo() : defaultOptions.getWordInfo());
		merged.setVoiceFormat(
				source.getVoiceFormat() != null ? source.getVoiceFormat() : defaultOptions.getVoiceFormat());
		return merged;
	}

}
