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

package io.github.studiousxiaoyu.hunyuan.embedding;

import io.github.studiousxiaoyu.hunyuan.HunYuanTestConfiguration;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanApi;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanEmbeddingModel;
import io.github.studiousxiaoyu.hunyuan.api.HunYuanEmbeddingOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = HunYuanTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_ID", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUNYUAN_SECRET_KEY", matches = ".+")
class EmbeddingIT{

	private Resource resource = new DefaultResourceLoader().getResource("classpath:text_source.txt");

	@Autowired
	private HunYuanEmbeddingModel embeddingModel;

	@Test
	void defaultEmbedding() {
		assertThat(this.embeddingModel).isNotNull();

		EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of("Hello World"));
		assertThat(embeddingResponse.getResults()).hasSize(1);
		assertThat(embeddingResponse.getResults().get(0)).isNotNull();
		assertThat(embeddingResponse.getResults().get(0).getOutput()).hasSize(1024);
		assertThat(embeddingResponse.getMetadata().getModel()).isEqualTo("hunyuan-embedding");
		assertThat(embeddingResponse.getMetadata().getUsage().getTotalTokens()).isEqualTo(5);
		assertThat(embeddingResponse.getMetadata().getUsage().getPromptTokens()).isEqualTo(5);

		assertThat(this.embeddingModel.dimensions()).isEqualTo(1024);
	}

	@Test
	void embeddingBatchDocuments() throws Exception {
		assertThat(this.embeddingModel).isNotNull();
		List<float[]> embeddings = this.embeddingModel.embed(
				List.of(new Document("Hello world"), new Document("Hello Spring"), new Document("Hello Spring AI!")),
				HunYuanEmbeddingOptions.builder().model(HunYuanApi.DEFAULT_EMBEDDING_MODEL).build(),
				new TokenCountBatchingStrategy());
		assertThat(embeddings.size()).isEqualTo(3);
		embeddings.forEach(embedding -> assertThat(embedding.length).isEqualTo(this.embeddingModel.dimensions()));
	}

	@Test
	void embeddingBatchDocumentsThatExceedTheLimit() throws Exception {
		assertThat(this.embeddingModel).isNotNull();
		String contentAsString = this.resource.getContentAsString(StandardCharsets.UTF_8);
		assertThatThrownBy(
				() -> this.embeddingModel.embed(List.of(new Document("Hello World"), new Document(contentAsString)),
						HunYuanEmbeddingOptions.builder().model(HunYuanApi.DEFAULT_EMBEDDING_MODEL).build(),
						new TokenCountBatchingStrategy()))
			.isInstanceOf(IllegalArgumentException.class);
	}

}
