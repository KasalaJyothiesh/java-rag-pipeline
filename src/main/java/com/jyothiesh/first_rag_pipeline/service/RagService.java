
package com.jyothiesh.first_rag_pipeline.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.PostConstruct;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import com.jyothiesh.first_rag_pipeline.assistant.Assistant;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

@Service
public class RagService {

    private final EmbeddingStoreIngestor ingestor;
    private final Assistant assistant;

    public RagService(
            EmbeddingStoreIngestor ingestor,
            Assistant assistant) {

        this.ingestor = ingestor;
        this.assistant = assistant;
    }

    @PostConstruct
    public void seedKnowledgeBase() throws IOException {

        var resolver = new PathMatchingResourcePatternResolver();

        Resource[] files = resolver.getResources(
                "classpath:documents/*.txt"
        );

        int count = 0;

        for (Resource file : files) {

            try (InputStream inputStream = file.getInputStream()) {

                String text = new String(
                        inputStream.readAllBytes(),
                        StandardCharsets.UTF_8
                );

                Document doc = Document.from(text);

                doc.metadata().put(
                        "source",
                        file.getFilename()
                );

                ingestor.ingest(doc);

                count++;

                System.out.println(
                        "[RAG] Ingested: " + file.getFilename()
                );
            }
        }

        System.out.println(
                "[RAG] Ingested " + count + " document(s)."
        );
    }

    public String ask(String question) {
        return assistant.answer(question);
    }
}
