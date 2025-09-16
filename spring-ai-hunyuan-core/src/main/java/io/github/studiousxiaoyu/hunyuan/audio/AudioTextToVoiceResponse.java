package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionResponseMetadata;
import org.springframework.ai.model.ModelResponse;

import java.util.List;

public class AudioTextToVoiceResponse implements ModelResponse<AudioTextToVoiceResult> {
    private final AudioTextToVoiceResult transcript;

    public AudioTextToVoiceResponse(AudioTextToVoiceResult transcript) {
        this.transcript = transcript;
    }

    public AudioTextToVoiceResult getResult() {
        return this.transcript;
    }

    public List<AudioTextToVoiceResult> getResults() {
        return List.of(this.transcript);
    }

    public AudioTranscriptionResponseMetadata getMetadata() {
        return null;
    }
}
