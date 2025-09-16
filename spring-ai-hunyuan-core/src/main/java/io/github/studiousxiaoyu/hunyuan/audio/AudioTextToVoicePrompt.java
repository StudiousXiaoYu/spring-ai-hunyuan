package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.model.ModelOptions;
import org.springframework.ai.model.ModelRequest;

public class AudioTextToVoicePrompt implements ModelRequest<String> {
    private final String text;

    public AudioTextToVoicePrompt(String audioResource) {
        this.text = audioResource;
    }

    public String getInstructions() {
        return this.text;
    }

    @Override
    public ModelOptions getOptions() {
        return null;
    }
}
