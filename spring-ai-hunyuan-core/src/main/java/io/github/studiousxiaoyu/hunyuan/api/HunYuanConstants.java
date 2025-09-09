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

/**
 * Constants for HunYuan API.
 *
 * @author Guo Junyu
 */
public final class HunYuanConstants {

	public static final String DEFAULT_BASE_URL = "https://hunyuan.tencentcloudapi.com";

	public static final String DEFAULT_TRANSCRIPTION_URL = "https://asr.tencentcloudapi.com";

	public static final String DEFAULT_CHAT_HOST = "hunyuan.tencentcloudapi.com";

	public static final String DEFAULT_TRANSCRIPTION_HOST = "asr.tencentcloudapi.com";

	public static final String PROVIDER_NAME = "HUNYUAN";

	public static final String DEFAULT_CHAT_ACTION = "ChatCompletions";

	public static final String DEFAULT_EMBED_ACTION = "GetEmbedding";
	public static final String DEFAULT_TRANSCRIPTION_ACTION = "SentenceRecognition";

	public static final String DEFAULT_VERSION = "2023-09-01";

	public static final String DEFAULT_SERVICE = "hunyuan";

	public static final String DEFAULT_TRANSCRIPTION_SERVICE = "asr";

	public static final String DEFAULT_ALGORITHM = "TC3-HMAC-SHA256";

	public static final String CT_JSON = "application/json; charset=utf-8";

	// 1：等待中、2：运行中、4：处理失败、5：处理完成。
	public static final String STATUS_WAITING = "1";

	public static final String STATUS_RUNNING = "2";

	public static final String STATUS_FAILED = "4";

	public static final String STATUS_SUCCESS = "5";

	public static final String IMAGE_RUNNING_MESSAGE = "正在生成中，请稍等...";

	private HunYuanConstants() {

	}

}
