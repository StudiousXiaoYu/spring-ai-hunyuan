package io.github.studiousxiaoyu.hunyuan.audio;

import io.github.studiousxiaoyu.hunyuan.api.HunYuanAudioApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscription;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.model.Model;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class HunYuanAudioTextToVoiceModel implements Model<AudioTextToVoicePrompt, AudioTextToVoiceResponse> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final RetryTemplate retryTemplate;

	private final HunYuanAudioApi hunYuanAudioApi;

	private final HunYuanAudioTextToVoiceModelOptions defaultOptions;

	public HunYuanAudioTextToVoiceModel(HunYuanAudioApi hunYuanAudioApi) {
		this(hunYuanAudioApi, HunYuanAudioTextToVoiceModelOptions.builder().build());
	}

	public HunYuanAudioTextToVoiceModel(HunYuanAudioApi hunYuanAudioApi, HunYuanAudioTextToVoiceModelOptions options) {
		this(RetryUtils.DEFAULT_RETRY_TEMPLATE, hunYuanAudioApi, options);
	}

	public HunYuanAudioTextToVoiceModel(RetryTemplate retryTemplate, HunYuanAudioApi hunYuanAudioApi,
			HunYuanAudioTextToVoiceModelOptions options) {
		this.retryTemplate = retryTemplate;
		this.hunYuanAudioApi = hunYuanAudioApi;
		this.defaultOptions = options;

	}

	public byte[] call(String text) {
		AudioTextToVoicePrompt transcriptionRequest = new AudioTextToVoicePrompt(text);
		return call(transcriptionRequest).getResult().getOutput();
	}

	@Override
	public AudioTextToVoiceResponse call(AudioTextToVoicePrompt transcriptionPrompt) {
		String textResource = transcriptionPrompt.getInstructions();
		HunYuanAudioApi.TextToVoiceRequest textToVoiceRequest = createRequest(transcriptionPrompt);
		ResponseEntity<HunYuanAudioApi.TextToVoiceResponse> transcriptionEntity = this.retryTemplate
			.execute(ctx -> this.hunYuanAudioApi.createTextToVoice(textToVoiceRequest,
					HunYuanAudioApi.TextToVoiceResponse.class));

		var transcription = transcriptionEntity.getBody();

		if (transcription == null) {
			logger.warn("No textToVoice returned for request: {}", textResource);
			return new AudioTextToVoiceResponse(null);
		}
		byte[] decode = Base64.getDecoder().decode(transcription.response().audio());
		AudioTextToVoiceResult transcript = new AudioTextToVoiceResult(decode);

		return new AudioTextToVoiceResponse(transcript);
	}

	private HunYuanAudioApi.TextToVoiceRequest createRequest(AudioTextToVoicePrompt transcriptionPrompt) {

		HunYuanAudioTextToVoiceModelOptions options = this.defaultOptions;

		if (transcriptionPrompt.getOptions() != null) {
			if (transcriptionPrompt.getOptions() instanceof HunYuanAudioTextToVoiceModelOptions runtimeOptions) {
				options = this.merge(runtimeOptions, options);
			}
			else {
				throw new IllegalArgumentException("Audio options are not of type TextToVoiceModelOptions: "
						+ transcriptionPrompt.getOptions().getClass().getSimpleName());
			}
		}

		String instructions = transcriptionPrompt.getInstructions();

		return HunYuanAudioApi.TextToVoiceRequest.builder()
			.text(instructions)
			.sessionId(UUID.randomUUID().toString())
			.volume(options.getVolume())
			.speed(options.getSpeed())
			.projectId(options.getProjectId())
			.modelType(options.getModelType())
			.voiceType(options.getVoiceType())
			.fastVoiceType(options.getFastVoiceType())
			.primaryLanguage(options.getPrimaryLanguage())
			.sampleRate(options.getSampleRate())
			.codec(options.getCodec())
			.enableSubtitle(options.getEnableSubtitle())
			.segmentRate(options.getSegmentRate())
			.emotionCategory(options.getEmotionCategory())
			.emotionIntensity(options.getEmotionIntensity())
			.build();
	}

	private String toBase64(InputStream inputStream) {
		try {
			return Base64.getEncoder().encodeToString(inputStream.readAllBytes()).replaceAll("\r|\n", "");
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private HunYuanAudioTextToVoiceModelOptions merge(HunYuanAudioTextToVoiceModelOptions source,
			HunYuanAudioTextToVoiceModelOptions defaultOptions) {
		if (source == null) {
			source = new HunYuanAudioTextToVoiceModelOptions();
		}
		HunYuanAudioTextToVoiceModelOptions merged = new HunYuanAudioTextToVoiceModelOptions();
		// 新增字段
		merged.setText(source.getText() != null ? source.getText() : defaultOptions.getText());
		merged.setSessionId(source.getSessionId() != null ? source.getSessionId() : defaultOptions.getSessionId());
		merged.setVolume(source.getVolume() != null ? source.getVolume() : defaultOptions.getVolume());
		merged.setSpeed(source.getSpeed() != null ? source.getSpeed() : defaultOptions.getSpeed());
		merged.setProjectId(source.getProjectId() != null ? source.getProjectId() : defaultOptions.getProjectId());
		merged.setModelType(source.getModelType() != null ? source.getModelType() : defaultOptions.getModelType());
		merged.setVoiceType(source.getVoiceType() != null ? source.getVoiceType() : defaultOptions.getVoiceType());
		merged.setFastVoiceType(
				source.getFastVoiceType() != null ? source.getFastVoiceType() : defaultOptions.getFastVoiceType());
		merged.setPrimaryLanguage(source.getPrimaryLanguage() != null ? source.getPrimaryLanguage()
				: defaultOptions.getPrimaryLanguage());
		merged.setSampleRate(source.getSampleRate() != null ? source.getSampleRate() : defaultOptions.getSampleRate());
		merged.setCodec(source.getCodec() != null ? source.getCodec() : defaultOptions.getCodec());
		merged.setEnableSubtitle(
				source.getEnableSubtitle() != null ? source.getEnableSubtitle() : defaultOptions.getEnableSubtitle());
		merged.setSegmentRate(
				source.getSegmentRate() != null ? source.getSegmentRate() : defaultOptions.getSegmentRate());
		merged.setEmotionCategory(source.getEmotionCategory() != null ? source.getEmotionCategory()
				: defaultOptions.getEmotionCategory());
		merged.setEmotionIntensity(source.getEmotionIntensity() != null ? source.getEmotionIntensity()
				: defaultOptions.getEmotionIntensity());
		return merged;
	}

}
