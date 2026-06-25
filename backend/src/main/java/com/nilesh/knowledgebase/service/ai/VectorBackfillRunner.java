package com.nilesh.knowledgebase.service.ai;

import com.nilesh.knowledgebase.entity.DocumentChunk;
import com.nilesh.knowledgebase.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("!test")
public class VectorBackfillRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VectorBackfillRunner.class);

    private final DocumentChunkRepository documentChunkRepository;
    private final VectorStoreService vectorStoreService;

    public VectorBackfillRunner(DocumentChunkRepository documentChunkRepository, VectorStoreService vectorStoreService) {
        this.documentChunkRepository = documentChunkRepository;
        this.vectorStoreService = vectorStoreService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for document chunks without embeddings...");
        List<DocumentChunk> allChunks = documentChunkRepository.findAll();
        List<DocumentChunk> chunksWithoutEmbeddings = allChunks.stream()
                .filter(chunk -> chunk.getEmbedding() == null)
                .collect(Collectors.toList());

        if (chunksWithoutEmbeddings.isEmpty()) {
            log.info("All document chunks already have embeddings.");
            return;
        }

        log.info("Found {} chunks without embeddings. Backfilling now...", chunksWithoutEmbeddings.size());
        vectorStoreService.saveEmbeddings(chunksWithoutEmbeddings);
        log.info("Backfill complete!");
    }
}
