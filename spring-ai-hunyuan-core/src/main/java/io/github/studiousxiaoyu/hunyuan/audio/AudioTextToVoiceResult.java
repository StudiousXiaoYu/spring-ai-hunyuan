package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionResponseMetadata;
import org.springframework.ai.model.ModelResponse;
import org.springframework.ai.model.ModelResult;
import org.springframework.ai.model.ResultMetadata;

import java.util.List;

public class AudioTextToVoiceResult implements ModelResult<String> {

    private final String text;

    public AudioTextToVoiceResult(String text) {
        this.text = text;
    }
    @Override
    public String getOutput() {
        return text;
    }

    @Override
    public ResultMetadata getMetadata() {
        return null;
    }
}
