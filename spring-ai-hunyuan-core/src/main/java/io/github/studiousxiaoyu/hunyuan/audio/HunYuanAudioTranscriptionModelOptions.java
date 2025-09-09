package io.github.studiousxiaoyu.hunyuan.audio;

import org.springframework.ai.audio.transcription.AudioTranscriptionOptions;

public class HunYuanAudioTranscriptionModelOptions implements AudioTranscriptionOptions {
    @Override
    public String getModel() {
        return "";
    }

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
}
