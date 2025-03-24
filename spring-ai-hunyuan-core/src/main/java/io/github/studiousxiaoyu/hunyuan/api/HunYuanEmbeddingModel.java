package io.github.studiousxiaoyu.hunyuan.api;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.embedding.observation.DefaultEmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationContext;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationDocumentation;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.util.List;

/**
 *
 * @version 1.0
 */
public class HunYuanEmbeddingModel extends AbstractEmbeddingModel {

    private static final Logger logger = LoggerFactory.getLogger(HunYuanEmbeddingModel.class);

    private static final EmbeddingModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultEmbeddingModelObservationConvention();

    private final HunYuanEmbeddingOptions defaultOptions;

    private final RetryTemplate retryTemplate;

    private final HunYuanApi hunYuanApi;

    private final MetadataMode metadataMode;

    /**
     * Observation registry used for instrumentation.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Conventions to use for generating observations.
     */
    private EmbeddingModelObservationConvention observationConvention = DEFAULT_OBSERVATION_CONVENTION;


    public HunYuanEmbeddingModel(HunYuanApi hunYuanApi) {
        this(hunYuanApi, MetadataMode.EMBED);
    }
    public HunYuanEmbeddingModel(HunYuanApi hunYuanApi, MetadataMode metadataMode) {
        this(hunYuanApi, metadataMode,
                HunYuanEmbeddingOptions.builder().model(HunYuanApi.DEFAULT_EMBEDDING_MODEL).build());
    }
    public HunYuanEmbeddingModel(HunYuanApi hunYuanApi, MetadataMode metadataMode,
                                 HunYuanEmbeddingOptions options) {
        this(options, RetryUtils.DEFAULT_RETRY_TEMPLATE,hunYuanApi,metadataMode);
    }

    public HunYuanEmbeddingModel(HunYuanEmbeddingOptions options, RetryTemplate retryTemplate, HunYuanApi hunYuanApi, MetadataMode metadataMode) {
        this(options, retryTemplate, hunYuanApi, metadataMode, ObservationRegistry.NOOP);
    }

    public HunYuanEmbeddingModel(HunYuanEmbeddingOptions options, RetryTemplate retryTemplate, HunYuanApi hunYuanApi, MetadataMode metadataMode, ObservationRegistry observationRegistry) {
        Assert.notNull(hunYuanApi, "hunYuanApi must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");
        Assert.notNull(observationRegistry, "observationRegistry must not be null");

        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
        this.hunYuanApi = hunYuanApi;
        this.metadataMode = metadataMode;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        HunYuanEmbeddingOptions requestOptions = mergeOptions(request.getOptions(), this.defaultOptions);
        HunYuanApi.EmbeddingRequest<List<String>> apiRequest = createRequest(request, requestOptions);

        var observationContext = EmbeddingModelObservationContext.builder()
                .embeddingRequest(request)
                .provider(HunYuanConstants.PROVIDER_NAME)
                .requestOptions(requestOptions)
                .build();

        return EmbeddingModelObservationDocumentation.EMBEDDING_MODEL_OPERATION
                .observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> observationContext,
                        this.observationRegistry)
                .observe(() -> {
                    HunYuanApi.EmbeddingList<HunYuanApi.Embedding> apiEmbeddingResponse = this.retryTemplate
                            .execute(ctx -> this.hunYuanApi.embeddings(apiRequest));

                    if (apiEmbeddingResponse == null) {
                        logger.warn("No embeddings returned for request: {}", request);
                        return new EmbeddingResponse(List.of());
                    }

                    var metadata = new EmbeddingResponseMetadata(requestOptions.getModel(),
                            getDefaultUsage(apiEmbeddingResponse.usage()));

                    List<Embedding> embeddings = apiEmbeddingResponse.data()
                            .stream()
                            .map(e -> new Embedding(e.embedding(), e.index()))
                            .toList();

                    EmbeddingResponse embeddingResponse = new EmbeddingResponse(embeddings, metadata);

                    observationContext.setResponse(embeddingResponse);

                    return embeddingResponse;
                });
    }


    @Override
    public float[] embed(Document document) {
        Assert.notNull(document, "Document must not be null");
        return this.embed(document.getFormattedContent(this.metadataMode));
    }

    private DefaultUsage getDefaultUsage(HunYuanApi.Usage usage) {
        return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
    }

    private HunYuanApi.EmbeddingRequest<List<String>> createRequest(EmbeddingRequest request,
                                                                    HunYuanEmbeddingOptions requestOptions) {
        return new HunYuanApi.EmbeddingRequest<>(request.getInstructions(),  requestOptions.getDimensions());
    }

    /**
     * Merge runtime and default {@link EmbeddingOptions} to compute the final options to
     * use in the request.
     */
    private HunYuanEmbeddingOptions mergeOptions(@Nullable EmbeddingOptions runtimeOptions,
                                                 HunYuanEmbeddingOptions defaultOptions) {
        var runtimeOptionsForProvider = ModelOptionsUtils.copyToTarget(runtimeOptions, EmbeddingOptions.class,
                HunYuanEmbeddingOptions.class);

        if (runtimeOptionsForProvider == null) {
            return defaultOptions;
        }

        return HunYuanEmbeddingOptions.builder()
                // Handle portable embedding options
                .model(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getModel(), defaultOptions.getModel()))
                .dimensions(ModelOptionsUtils.mergeOption(runtimeOptionsForProvider.getDimensions(),
                        defaultOptions.getDimensions()))
                .build();
    }
}
