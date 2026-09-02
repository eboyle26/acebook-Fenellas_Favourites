package com.makersacademy.acebook.controller;

import com.makersacademy.acebook.service.MusicSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MusicController {

    private final MusicSearchService musicSearchService;

    public MusicController(MusicSearchService musicSearchService) {
        this.musicSearchService = musicSearchService;
    }

    @GetMapping("/songs/search")
    public List<Map<String, Object>> searchSongs(
            @RequestParam String query
    ) {
        return musicSearchService.searchSongs(query);
    }
}