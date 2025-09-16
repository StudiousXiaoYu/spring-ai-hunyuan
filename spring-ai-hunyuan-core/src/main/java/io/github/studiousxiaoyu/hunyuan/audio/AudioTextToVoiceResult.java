package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

public class AudioTextToVoiceResult implements ModelResult<byte[]> {

	private final byte[] text;

	public AudioTextToVoiceResult(byte[] text) {
		this.text = text;
	}

	@Override
	public byte[] getOutput() {
		return text;
	}

	@Override
	public ResultMetadata getMetadata() {
		return null;
	}

}
