package com.nilesh.knowledgebase.controller;



import com.nilesh.knowledgebase.dto.SemanticSearchResult;

import com.nilesh.knowledgebase.security.UserPrincipal;

import com.nilesh.knowledgebase.service.ai.UnifiedSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final UnifiedSearchService unifiedSearchService;

    @GetMapping
    public List<SemanticSearchResult> search(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "hybrid") String mode) {
        return unifiedSearchService.searchWithScores(user.getId(), query, limit, mode);
    }
}

