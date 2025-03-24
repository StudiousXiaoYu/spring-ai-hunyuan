/*
 * Copyright 2023-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.github.studiousxiaoyu.hunyuan.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

/**
 * HunYuan Embedding Options.
 *
 * @author Guo Junyu
 */
@JsonInclude(Include.NON_NULL)
public class HunYuanEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off
	/**
	 * ID of the model to use.
	 */
	private @JsonProperty("model") String model;
	/**
	 * The number of dimensions the resulting output embeddings should have. Only supported in text-embedding-3 and later models.
	 */
	private @JsonProperty("dimensions") Integer dimensions;

	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Integer getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}


	public static class Builder {

		protected HunYuanEmbeddingOptions options;

		public Builder() {
			this.options = new HunYuanEmbeddingOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder dimensions(Integer dimensions) {
			this.options.dimensions = dimensions;
			return this;
		}
		public HunYuanEmbeddingOptions build() {
			return this.options;
		}

	}

}
