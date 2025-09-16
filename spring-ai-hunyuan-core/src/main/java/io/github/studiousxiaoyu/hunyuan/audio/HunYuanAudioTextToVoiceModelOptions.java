package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;

import java.util.Objects;

public class HunYuanAudioTextToVoiceModelOptions {

    // 合成语音的源文本
    private String text;

    // 一次请求对应一个SessionId
    private String sessionId;

    // 音量大小，范围[-10，10]，默认为0
    private Float volume;

    // 语速，范围：[-2，6]，默认为0
    private Float speed;

    // 项目id，用户自定义，默认为0
    private Integer projectId;

    // 模型类型，1-默认模型
    private Integer modelType;

    // 音色 ID
    private Integer voiceType;

    // 一句话版声音复刻音色ID
    private String fastVoiceType;

    // 主语言类型：1-中文（默认）2-英文 3-日文
    private Integer primaryLanguage;

    // 音频采样率：24000/16000（默认）/8000
    private Integer sampleRate;

    // 返回音频格式：wav（默认）/mp3/pcm
    private String codec;

    // 是否开启时间戳功能，默认为false
    private Boolean enableSubtitle;

    // 断句敏感阈值，默认值为：0，取值范围：[0,1,2]
    private Integer segmentRate;

    // 控制合成音频的情感
    private String emotionCategory;

    // 控制合成音频情感程度，取值范围为[50,200],默认为100
    private Integer emotionIntensity;

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getModelType() {
        return modelType;
    }

    public void setModelType(Integer modelType) {
        this.modelType = modelType;
    }

    public Integer getVoiceType() {
        return voiceType;
    }

    public void setVoiceType(Integer voiceType) {
        this.voiceType = voiceType;
    }

    public String getFastVoiceType() {
        return fastVoiceType;
    }

    public void setFastVoiceType(String fastVoiceType) {
        this.fastVoiceType = fastVoiceType;
    }

    public Integer getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(Integer primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public Integer getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public Boolean getEnableSubtitle() {
        return enableSubtitle;
    }

    public void setEnableSubtitle(Boolean enableSubtitle) {
        this.enableSubtitle = enableSubtitle;
    }

    public Integer getSegmentRate() {
        return segmentRate;
    }

    public void setSegmentRate(Integer segmentRate) {
        this.segmentRate = segmentRate;
    }

    public String getEmotionCategory() {
        return emotionCategory;
    }

    public void setEmotionCategory(String emotionCategory) {
        this.emotionCategory = emotionCategory;
    }

    public Integer getEmotionIntensity() {
        return emotionIntensity;
    }

    public void setEmotionIntensity(Integer emotionIntensity) {
        this.emotionIntensity = emotionIntensity;
    }

    public static class Builder {
        private HunYuanAudioTextToVoiceModelOptions options;
        public Builder() {
            options = new HunYuanAudioTextToVoiceModelOptions();
        }
        public Builder text(String text) {
            options.setText(text);
            return this;
        }

        public Builder sessionId(String sessionId) {
            options.setSessionId(sessionId);
            return this;
        }

        public Builder volume(Float volume) {
            options.setVolume(volume);
            return this;
        }

        public Builder speed(Float speed) {
            options.setSpeed(speed);
            return this;
        }

        public Builder projectId(Integer projectId) {
            options.setProjectId(projectId);
            return this;
        }

        public Builder modelType(Integer modelType) {
            options.setModelType(modelType);
            return this;
        }

        public Builder voiceType(Integer voiceType) {
            options.setVoiceType(voiceType);
            return this;
        }

        public Builder fastVoiceType(String fastVoiceType) {
            options.setFastVoiceType(fastVoiceType);
            return this;
        }

        public Builder primaryLanguage(Integer primaryLanguage) {
            options.setPrimaryLanguage(primaryLanguage);
            return this;
        }

        public Builder sampleRate(Integer sampleRate) {
            options.setSampleRate(sampleRate);
            return this;
        }

        public Builder codec(String codec) {
            options.setCodec(codec);
            return this;
        }

        public Builder enableSubtitle(Boolean enableSubtitle) {
            options.setEnableSubtitle(enableSubtitle);
            return this;
        }

        public Builder segmentRate(Integer segmentRate) {
            options.setSegmentRate(segmentRate);
            return this;
        }

        public Builder emotionCategory(String emotionCategory) {
            options.setEmotionCategory(emotionCategory);
            return this;
        }

        public Builder emotionIntensity(Integer emotionIntensity) {
            options.setEmotionIntensity(emotionIntensity);
            return this;
        }
        public HunYuanAudioTextToVoiceModelOptions build() {
            return options;
        }
    }
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof HunYuanAudioTextToVoiceModelOptions that)) return false;

        return Objects.equals(text, that.text) && Objects.equals(sessionId, that.sessionId) && Objects.equals(volume, that.volume) && Objects.equals(speed, that.speed) && Objects.equals(projectId, that.projectId) && Objects.equals(modelType, that.modelType) && Objects.equals(voiceType, that.voiceType) && Objects.equals(fastVoiceType, that.fastVoiceType) && Objects.equals(primaryLanguage, that.primaryLanguage) && Objects.equals(sampleRate, that.sampleRate) && Objects.equals(codec, that.codec) && Objects.equals(enableSubtitle, that.enableSubtitle) && Objects.equals(segmentRate, that.segmentRate) && Objects.equals(emotionCategory, that.emotionCategory) && Objects.equals(emotionIntensity, that.emotionIntensity);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(text);
        result = 31 * result + Objects.hashCode(sessionId);
        result = 31 * result + Objects.hashCode(volume);
        result = 31 * result + Objects.hashCode(speed);
        result = 31 * result + Objects.hashCode(projectId);
        result = 31 * result + Objects.hashCode(modelType);
        result = 31 * result + Objects.hashCode(voiceType);
        result = 31 * result + Objects.hashCode(fastVoiceType);
        result = 31 * result + Objects.hashCode(primaryLanguage);
        result = 31 * result + Objects.hashCode(sampleRate);
        result = 31 * result + Objects.hashCode(codec);
        result = 31 * result + Objects.hashCode(enableSubtitle);
        result = 31 * result + Objects.hashCode(segmentRate);
        result = 31 * result + Objects.hashCode(emotionCategory);
        result = 31 * result + Objects.hashCode(emotionIntensity);
        return result;
    }
}
