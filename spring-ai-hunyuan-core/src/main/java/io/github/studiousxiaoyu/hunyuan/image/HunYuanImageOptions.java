package io.github.studiousxiaoyu.hunyuan.image;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.image.ImageOptions;

/**
 * HunYuan Image API options. HunYuanImageOptions.java
 *
 * @author guo junyu
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HunYuanImageOptions implements ImageOptions {

	/**
	 * 图片生成数量。
	 * 支持1 ~ 4张，默认生成1张。
	 * 示例值：1
	 */
	@JsonProperty("Num")
	private Integer num;

	/**
	 * 生成图分辨率。
	 * 支持生成以下分辨率的图片：768:768（1:1）、768:1024（3:4）、1024:768（4:3）、1024:1024（1:1）、720:1280（9:16）、1280:720（16:9）、768:1280（3:5）、1280:768（5:3），不传默认使用1024:1024。
	 * 如果上传 ContentImage 参考图，分辨率仅支持：768:768（1:1）、768:1024（3:4）、1024:768（4:3）、1024:1024（1:1），不传将自动适配分辨率。如果参考图被用于做风格转换，将生成保持原图长宽比例且长边为1024的图片，指定的分辨率不生效。
	 * 示例值：1024:1024
	 */
	@JsonProperty("Resolution")
	private String resolution;
	/**
	 * 绘画风格。
	 * 请在 混元生图风格列表 中选择期望的风格，传入风格编号。
	 * 不传默认不指定风格。
	 * 示例值：riman
	 */
	@JsonProperty("Style")
	private String style;

	/**
	 * 绘画风格。
	 * 请在 混元生图风格列表 中选择期望的风格，传入风格编号。
	 * 不传默认不指定风格。
	 * 示例值：riman
	 */
	@JsonProperty("Style")
	private String Style;

	/**
	 * 超分选项，默认不做超分，可选开启。
	 * x2：2倍超分
	 * x4：4倍超分
	 * 在 Resolution 的基础上按比例提高分辨率，例如1024:1024开启2倍超分后将得到2048:2048。
	 * 示例值：x2
	 */
	@JsonProperty("Clarity")
	private String clarity;

	/**
	 * prompt 扩写开关。1为开启，0为关闭，不传默认开启。
	 * 开启扩写后，将自动扩写原始输入的 prompt 并使用扩写后的 prompt 生成图片，返回生成图片结果时将一并返回扩写后的 prompt 文本。
	 * 如果关闭扩写，将直接使用原始输入的 prompt 生成图片。如果上传了参考图，扩写关闭不生效，将保持开启。
	 * 建议开启，在多数场景下可提升生成图片效果、丰富生成图片细节。
	 * 示例值：0
	 */
	@JsonProperty("Revise")
	private Integer revise;

	/**
	 * 随机种子，默认随机。
	 * 不传：随机种子生成。
	 * 正数：固定种子生成。
	 * 扩写开启时固定种子不生效，将保持随机。
	 * 示例值：1
	 */
	@JsonProperty("Seed")
	private Integer seed;

	/**
	 * 为生成结果图添加显式水印标识的开关，默认为1。
	 * 1：添加。
	 * 0：不添加。
	 * 其他数值：默认按1处理。
	 * 建议您使用显著标识来提示结果图使用了 AI 绘画技术，是 AI 生成的图片。
	 * 示例值：1
	 */
	@JsonProperty("LogoAdd")
	private Integer logoAdd;

	/**
	 * 标识内容设置。
	 * 默认在生成结果图右下角添加“图片由 AI 生成”字样，您可根据自身需要替换为其他的标识图片。
	 */
	@JsonProperty("LogoParam")
	private String logoParam;

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Create a new OpenAiImageOptions instance from an existing one.
	 * @param fromOptions The options to copy from
	 * @return A new OpenAiImageOptions instance
	 */
	public static HunYuanImageOptions fromOptions(HunYuanImageOptions fromOptions) {
		HunYuanImageOptions options = new HunYuanImageOptions();
		options.n = fromOptions.n;
		options.model = fromOptions.model;
		options.width = fromOptions.width;
		options.height = fromOptions.height;
		options.quality = fromOptions.quality;
		options.responseFormat = fromOptions.responseFormat;
		options.size = fromOptions.size;
		options.style = fromOptions.style;
		options.user = fromOptions.user;
		return options;
	}

	@Override
	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getQuality() {
		return this.quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	@Override
	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	@Override
	public Integer getWidth() {
		if (this.width != null) {
			return this.width;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[0]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setWidth(Integer width) {
		this.width = width;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	@Override
	public Integer getHeight() {
		if (this.height != null) {
			return this.height;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[1]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setHeight(Integer height) {
		this.height = height;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	@Override
	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getSize() {
		if (this.size != null) {
			return this.size;
		}
		return (this.width != null && this.height != null) ? this.width + "x" + this.height : null;
	}

	public void setSize(String size) {
		this.size = size;

		// Parse the size string to update width and height
		if (size != null) {
			try {
				String[] dimensions = size.split("x");
				if (dimensions.length == 2) {
					this.width = Integer.parseInt(dimensions[0]);
					this.height = Integer.parseInt(dimensions[1]);
				}
			}
			catch (Exception ex) {
				// If parsing fails, leave width and height unchanged
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof HunYuanImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height)
				&& Objects.equals(this.quality, that.quality)
				&& Objects.equals(this.responseFormat, that.responseFormat) && Objects.equals(this.size, that.size)
				&& Objects.equals(this.style, that.style) && Objects.equals(this.user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.width, this.height, this.quality, this.responseFormat, this.size,
				this.style, this.user);
	}

	@Override
	public String toString() {
		return "OpenAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", width=" + this.width
				+ ", height=" + this.height + ", quality='" + this.quality + '\'' + ", responseFormat='"
				+ this.responseFormat + '\'' + ", size='" + this.size + '\'' + ", style='" + this.style + '\''
				+ ", user='" + this.user + '\'' + '}';
	}

	/**
	 * Create a copy of this options instance.
	 * @return A new instance with the same options
	 */
	public HunYuanImageOptions copy() {
		return fromOptions(this);
	}

	public static final class Builder {

		protected HunYuanImageOptions options;

		public Builder() {
			this.options = new HunYuanImageOptions();
		}

		public Builder(HunYuanImageOptions options) {
			this.options = options;
		}

		public Builder N(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder quality(String quality) {
			this.options.setQuality(quality);
			return this;
		}

		public Builder responseFormat(String responseFormat) {
			this.options.setResponseFormat(responseFormat);
			return this;
		}

		public Builder width(Integer width) {
			this.options.setWidth(width);
			return this;
		}

		public Builder height(Integer height) {
			this.options.setHeight(height);
			return this;
		}

		public Builder style(String style) {
			this.options.setStyle(style);
			return this;
		}

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public HunYuanImageOptions build() {
			return this.options;
		}

	}

}
