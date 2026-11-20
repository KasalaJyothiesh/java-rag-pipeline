package com.jyothiesh.first_rag_pipeline.configuration;
import com.jyothiesh.first_rag_pipeline.assistant.Assistant;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagConfiguration {
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model-name}")
    private String modelName;

    // PGVector Configuration
    @Value("${rag.pgvector.host}")
    private String host;

    @Value("${rag.pgvector.port}")
    private int port;

    @Value("${rag.pgvector.database}")
    private String database;

    @Value("${rag.pgvector.user}")
    private String user;

    @Value("${rag.pgvector.password}")
    private String password;

    @Value("${rag.pgvector.table}")
    private String table;

    @Value("${gemini.embedding.model-name}")
    private String embeddingModel;

    @Value("${gemini.embedding.dimensions}")
    private int dimension;

    @Bean
    public ChatModel chatModel()
    {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    //embedding model to embedd the text chunks
    @Bean
    public EmbeddingModel embeddingModel()
    {
        return GoogleAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName(embeddingModel)
                .outputDimensionality(dimension)
                .build();
    }

    //embedding store to keep the embeddings
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore()
    {
        return PgVectorEmbeddingStore.builder()
                .host(host)
                .user(user)
                .password(password)
                .port(port)
                .database(database)
                .table(table)
                .dimension(dimension)
                .build();
    }

    //Ingestor to make doc->split(tokens)->embeddings->embedding-store
    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore
    ) {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(100,10))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    //Retrive the relevant chunks
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel
    ){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(10)
                .minScore(0.0)
                .build();
    }

    //Connect Ai Service ----> RAG
    @Bean
    Assistant assistant(ChatModel chatModel, ContentRetriever contentRetriever)
    {
        return AiServices.builder(Assistant.class)
                .contentRetriever(contentRetriever)
                .chatModel(chatModel)
                .build();
    }

}
