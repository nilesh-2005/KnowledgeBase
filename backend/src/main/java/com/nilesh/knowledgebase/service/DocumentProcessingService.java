package com.nilesh.knowledgebase.service;

import com.nilesh.knowledgebase.entity.Document;
import com.nilesh.knowledgebase.entity.DocumentChunk;
import com.nilesh.knowledgebase.entity.DocumentStatus;
import com.nilesh.knowledgebase.repository.DocumentChunkRepository;
import com.nilesh.knowledgebase.repository.DocumentRepository;
import com.nilesh.knowledgebase.service.ai.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final StorageService storageService;
    private final VectorStoreService vectorStoreService;

    // Simple chunking parameters
    private static final int CHUNK_SIZE = 1000;
    private static final int CHUNK_OVERLAP = 200;

    @Async("taskExecutor")
    @Transactional
    public void processDocumentAsync(UUID documentId) {
        log.info("[Pipeline] ASYNC_STARTED  documentId={} thread={}", documentId, Thread.currentThread().getName());

        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.error("[Pipeline] DOCUMENT_NOT_FOUND  documentId={}", documentId);
            return;
        }

        try {
            // Step 1: Mark as PROCESSING
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);
            log.info("[Pipeline] STATUS->PROCESSING  documentId={}", documentId);

            // Step 2: Extract text via Apache Tika
            Resource resource = storageService.loadAsResource(document.getStoragePath());
            String extractedText;
            try (InputStream is = resource.getInputStream()) {
                Tika tika = new Tika();
                extractedText = tika.parseToString(is);
            }
            log.info("[Pipeline] TEXT_EXTRACTED  documentId={} rawLength={}", documentId,
                    extractedText == null ? 0 : extractedText.length());

            if (extractedText == null || extractedText.trim().isEmpty()) {
                throw new IllegalStateException("Extracted text is empty - file may be image-only or corrupted");
            }

            // Clean up the text
            extractedText = extractedText.replaceAll("\\s+", " ").trim();

            // Step 3: Generate chunks with positional data
            List<ChunkData> textChunks = splitIntoChunks(extractedText);
            log.info("[Pipeline] CHUNKS_GENERATED  documentId={} count={}", documentId, textChunks.size());

            // Step 4: Persist chunks
            List<DocumentChunk> chunksToSave = new ArrayList<>();
            for (int i = 0; i < textChunks.size(); i++) {
                ChunkData chunkData = textChunks.get(i);
                String chunkContent = chunkData.content();
                DocumentChunk chunk = DocumentChunk.builder()
                        .document(document)
                        .chunkIndex(i)
                        .content(chunkContent)
                        .tokenCount(estimateTokenCount(chunkContent))
                        .characterStart(chunkData.start())
                        .characterEnd(chunkData.end())
                        .build();
                chunksToSave.add(chunk);
            }

            documentChunkRepository.saveAll(chunksToSave);
            log.info("[Pipeline] CHUNKS_PERSISTED  documentId={} count={}", documentId, chunksToSave.size());

            // Step 5: Generate and persist embeddings
            log.info("[Pipeline] GENERATING_EMBEDDINGS  documentId={} count={}", documentId, chunksToSave.size());
            vectorStoreService.saveEmbeddings(chunksToSave);
            log.info("[Pipeline] EMBEDDINGS_PERSISTED  documentId={} count={}", documentId, chunksToSave.size());

            // Step 6: Mark as READY
            document.setStatus(DocumentStatus.READY);
            document.setErrorMessage(null);
            documentRepository.save(document);
            log.info("[Pipeline] STATUS->READY  documentId={}", documentId);

        } catch (Exception e) {
            log.error("[Pipeline] PROCESSING_FAILED  documentId={} error={}", documentId, e.getMessage(), e);
            try {
                document.setStatus(DocumentStatus.FAILED);
                document.setErrorMessage(e.getMessage());
                documentRepository.save(document);
                log.info("[Pipeline] STATUS->FAILED  documentId={}", documentId);
            } catch (Exception saveEx) {
                log.error("[Pipeline] FAILED_TO_SAVE_ERROR_STATUS  documentId={} error={}", documentId, saveEx.getMessage(), saveEx);
            }
        }
    }

    private record ChunkData(String content, int start, int end) {}

    private List<ChunkData> splitIntoChunks(String text) {
        List<ChunkData> chunks = new ArrayList<>();
        int length = text.length();
        int i = 0;

        while (i < length) {
            int end = Math.min(i + CHUNK_SIZE, length);

            if (end < length) {
                int lastSpace = text.lastIndexOf(" ", end);
                if (lastSpace > i) {
                    end = lastSpace;
                }
            }

            chunks.add(new ChunkData(text.substring(i, end).trim(), i, end));

            i = end - CHUNK_OVERLAP;
            if (i < 0) i = 0;

            if (end >= length) break;
        }

        return chunks;
    }

    private int estimateTokenCount(String text) {
        return text.length() / 4;
    }
}
