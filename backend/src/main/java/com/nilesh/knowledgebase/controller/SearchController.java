package com.nilesh.knowledgebase.controller;



import com.nilesh.knowledgebase.dto.SemanticSearchResult;

import com.nilesh.knowledgebase.security.UserPrincipal;

import com.nilesh.knowledgebase.service.ai.SimilaritySearchService;

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



    private final SimilaritySearchService similaritySearchService;



    @GetMapping

    public List<SemanticSearchResult> search(

            @AuthenticationPrincipal UserPrincipal user,

            @RequestParam("q") String query,

            @RequestParam(defaultValue = "10") int limit) {

        return similaritySearchService.searchWithScores(user.getId(), query, limit);

    }

}

