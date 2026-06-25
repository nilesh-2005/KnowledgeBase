package com.nilesh.knowledgebase.service.ai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetrievalConfidenceTest {

    @Test
    void classifyReturnsHighWhenTopScoreAtOrAbove075() {
        assertEquals("high", RetrievalConfidence.classify(0.75));
        assertEquals("high", RetrievalConfidence.classify(0.91));
    }

    @Test
    void classifyReturnsMediumWhenTopScoreAtOrAbove060() {
        assertEquals("medium", RetrievalConfidence.classify(0.60));
        assertEquals("medium", RetrievalConfidence.classify(0.74));
    }

    @Test
    void classifyReturnsLowWhenTopScoreAtOrAbove045() {
        assertEquals("low", RetrievalConfidence.classify(0.45));
        assertEquals("low", RetrievalConfidence.classify(0.59));
    }

    @Test
    void classifyReturnsNoneWhenTopScoreBelow045() {
        assertEquals("none", RetrievalConfidence.classify(0.44));
        assertEquals("none", RetrievalConfidence.classify(0.0));
    }

    @Test
    void isRefusalAnswerMatchesExactRefusalMessage() {
        assertTrue(RetrievalConfidence.isRefusalAnswer(
                "I don't have enough information in the knowledge base to answer that."));
        assertFalse(RetrievalConfidence.isRefusalAnswer("Mars has no capital."));
    }
}
