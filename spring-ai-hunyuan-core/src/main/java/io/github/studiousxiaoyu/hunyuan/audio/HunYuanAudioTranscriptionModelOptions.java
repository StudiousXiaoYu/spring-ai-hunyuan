package io.github.studiousxiaoyu.hunyuan.audio;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;

import java.util.Objects;

public class HunYuanAudioTranscriptionModelOptions implements AudioTranscriptionOptions {

    private String model;
    private String engSerViceType;
    private String sourceType;
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
    @Override
    public String getModel() {
        return model;
    }
    public String getEngSerViceType() {
        return engSerViceType;
    }

    public void setEngSerViceType(String engSerViceType) {
        this.engSerViceType = engSerViceType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getVoiceFormat() {
        return voiceFormat;
    }

    public void setVoiceFormat(String voiceFormat) {
        this.voiceFormat = voiceFormat;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Integer getDataLen() {
        return dataLen;
    }

    public void setDataLen(Integer dataLen) {
        this.dataLen = dataLen;
    }

    public Integer getWordInfo() {
        return wordInfo;
    }

    public void setWordInfo(Integer wordInfo) {
        this.wordInfo = wordInfo;
    }

    public Integer getFilterDirty() {
        return filterDirty;
    }

    public void setFilterDirty(Integer filterDirty) {
        this.filterDirty = filterDirty;
    }

    public Integer getFilterModal() {
        return filterModal;
    }

    public void setFilterModal(Integer filterModal) {
        this.filterModal = filterModal;
    }

    public Integer getFilterPunc() {
        return filterPunc;
    }

    public void setFilterPunc(Integer filterPunc) {
        this.filterPunc = filterPunc;
    }

    public Integer getConvertNumMode() {
        return convertNumMode;
    }

    public void setConvertNumMode(Integer convertNumMode) {
        this.convertNumMode = convertNumMode;
    }

    public String getHotwordId() {
        return hotwordId;
    }

    public void setHotwordId(String hotwordId) {
        this.hotwordId = hotwordId;
    }

    public String getCustomizationId() {
        return customizationId;
    }

    public void setCustomizationId(String customizationId) {
        this.customizationId = customizationId;
    }

    public String getHotwordList() {
        return hotwordList;
    }

    public void setHotwordList(String hotwordList) {
        this.hotwordList = hotwordList;
    }

    public String getInputSampleRate() {
        return inputSampleRate;
    }

    public void setInputSampleRate(String inputSampleRate) {
        this.inputSampleRate = inputSampleRate;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HunYuanAudioTranscriptionModelOptions that = (HunYuanAudioTranscriptionModelOptions) o;
        return Objects.equals(engSerViceType, that.engSerViceType) && Objects.equals(sourceType, that.sourceType) && Objects.equals(voiceFormat, that.voiceFormat) && Objects.equals(url, that.url) && Objects.equals(data, that.data) && Objects.equals(dataLen, that.dataLen) && Objects.equals(wordInfo, that.wordInfo) && Objects.equals(filterDirty, that.filterDirty) && Objects.equals(filterModal, that.filterModal) && Objects.equals(filterPunc, that.filterPunc) && Objects.equals(convertNumMode, that.convertNumMode) && Objects.equals(hotwordId, that.hotwordId) && Objects.equals(customizationId, that.customizationId) && Objects.equals(hotwordList, that.hotwordList) && Objects.equals(inputSampleRate, that.inputSampleRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engSerViceType, sourceType, voiceFormat, url, data, dataLen, wordInfo, filterDirty, filterModal, filterPunc, convertNumMode, hotwordId, customizationId, hotwordList, inputSampleRate);
    }

    public static class Builder {
        private HunYuanAudioTranscriptionModelOptions options;
        public Builder() {
            options = new HunYuanAudioTranscriptionModelOptions();
        }
        public Builder withModel(String model) {
            options.setModel(model);
            return this;
        }
        public Builder withEngSerViceType(String engSerViceType) {
            options.setEngSerViceType(engSerViceType);
            return this;
        }
        public Builder withSourceType(String sourceType) {
            options.setSourceType(sourceType);
            return this;
        }
        public Builder withVoiceFormat(String voiceFormat) {
            options.setVoiceFormat(voiceFormat);
            return this;
        }
        public Builder withUrl(String url) {
            options.setUrl(url);
            return this;
        }
        public Builder withData(String data) {
            options.setData(data);
            return this;
        }
        public Builder withDataLen(Integer dataLen) {
            options.setDataLen(dataLen);
            return this;
        }
        public Builder withWordInfo(Integer wordInfo) {
            options.setWordInfo(wordInfo);
            return this;
        }
        public Builder withFilterDirty(Integer filterDirty) {
            options.setFilterDirty(filterDirty);
            return this;
        }
        public Builder withFilterModal(Integer filterModal) {
            options.setFilterModal(filterModal);
            return this;
        }
        public Builder withFilterPunc(Integer filterPunc) {
            options.setFilterPunc(filterPunc);
            return this;
        }
        public Builder withConvertNumMode(Integer convertNumMode) {
            options.setConvertNumMode(convertNumMode);
            return this;
        }
        public Builder withHotwordId(String hotwordId) {
            options.setHotwordId(hotwordId);
            return this;
        }
        public Builder withCustomizationId(String customizationId) {
            options.setCustomizationId(customizationId);
            return this;
        }
        public Builder withHotwordList(String hotwordList) {
            options.setHotwordList(hotwordList);
            return this;
        }
        public Builder withInputSampleRate(String inputSampleRate) {
            options.setInputSampleRate(inputSampleRate);
            return this;
        }
        public HunYuanAudioTranscriptionModelOptions build() {
            return options;
        }
    }
    public static Builder builder() {
        return new Builder();
    }
}
