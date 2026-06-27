package com.nilesh.knowledgebase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "app.rag")
public class RagProperties {

    @Min(1)
    @Max(10)
    private int topK = 5;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double scoreThreshold = 0.35;

    @Min(1)
    @Max(5)
    private int fetchMultiplier = 2;

    @Min(1000)
    private int maxContextChars = 12000;

    @Min(50)
    private int excerptLength = 200;

    @Min(10)
    private int llmTimeoutSeconds = 120;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double semanticWeight = 0.70;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double keywordWeight = 0.30;

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public double getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(double scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public int getFetchMultiplier() {
        return fetchMultiplier;
    }

    public void setFetchMultiplier(int fetchMultiplier) {
        this.fetchMultiplier = fetchMultiplier;
    }

    public int getMaxContextChars() {
        return maxContextChars;
    }

    public void setMaxContextChars(int maxContextChars) {
        this.maxContextChars = maxContextChars;
    }

    public int getExcerptLength() {
        return excerptLength;
    }

    public void setExcerptLength(int excerptLength) {
        this.excerptLength = excerptLength;
    }

    public int getLlmTimeoutSeconds() {
        return llmTimeoutSeconds;
    }

    public void setLlmTimeoutSeconds(int llmTimeoutSeconds) {
        this.llmTimeoutSeconds = llmTimeoutSeconds;
    }

    public double getSemanticWeight() {
        return semanticWeight;
    }

    public void setSemanticWeight(double semanticWeight) {
        this.semanticWeight = semanticWeight;
    }

    public double getKeywordWeight() {
        return keywordWeight;
    }

    public void setKeywordWeight(double keywordWeight) {
        this.keywordWeight = keywordWeight;
    }
}
