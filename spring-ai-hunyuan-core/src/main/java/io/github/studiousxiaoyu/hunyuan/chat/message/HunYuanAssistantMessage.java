package io.github.studiousxiaoyu.hunyuan.chat.message;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HunYuanAssistantMessage extends AssistantMessage {

	private String reasoningContent;

	public HunYuanAssistantMessage(String content) {
		super(content);
	}

	public HunYuanAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls,
			List<Media> media) {
		super(content, properties, toolCalls, media);
	}

	public HunYuanAssistantMessage(String content, Map<String, Object> properties, List<ToolCall> toolCalls) {
		super(content, properties, toolCalls);
	}

	public HunYuanAssistantMessage(String content, Map<String, Object> properties) {
		super(content, properties);
	}

	public HunYuanAssistantMessage(String content, String reasoningContent, Map<String, Object> properties,
			List<ToolCall> toolCalls) {
		super(content, properties, toolCalls);
		this.reasoningContent = reasoningContent;
	}

	public String getReasoningContent() {
		return reasoningContent;
	}

	public void setReasoningContent(String reasoningContent) {
		this.reasoningContent = reasoningContent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		if (!super.equals(o))
			return false;
		HunYuanAssistantMessage that = (HunYuanAssistantMessage) o;
		return Objects.equals(reasoningContent, that.reasoningContent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), reasoningContent);
	}

	@Override
	public String toString() {
		return "HunYuanAssistantMessage{" + "reasoningContent='" + reasoningContent + '\'' + ", media=" + media
				+ ", messageType=" + messageType + ", textContent='" + textContent + '\'' + ", metadata=" + metadata
				+ '}';
	}

}
