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

package io.github.studiousxiaoyu.hunyuan.metadata;

import org.springframework.ai.chat.metadata.Usage;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import org.springframework.util.Assert;

/**
 * Represents the usage of a HunYuan model.
 *
 * @author Guo Junyu
 */
public class HunYuanUsage implements Usage {

	private final HunYuanApi.Usage usage;

	protected HunYuanUsage(HunYuanApi.Usage usage) {
		Assert.notNull(usage, "Hunyuan Usage must not be null");
		this.usage = usage;
	}

	public static HunYuanUsage from(HunYuanApi.Usage usage) {
		return new HunYuanUsage(usage);
	}

	protected HunYuanApi.Usage getUsage() {
		return this.usage;
	}

	@Override
	public Integer getPromptTokens() {
		return getUsage().promptTokens();
	}

	@Override
	public Integer getCompletionTokens() {
		return getUsage().completionTokens();
	}

	@Override
	public Object getNativeUsage() {
		return null;
	}

	@Override
	public String toString() {
		return getUsage().toString();
	}

}
