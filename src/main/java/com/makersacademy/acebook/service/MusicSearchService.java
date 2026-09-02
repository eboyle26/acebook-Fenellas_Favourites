package com.makersacademy.acebook.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class MusicSearchService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MusicSearchService(ObjectMapper objectMapper) {
        restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public List<Map<String, Object>> searchSongs(String query) {

        try {
            String response = restClient
                    .get()
                    .uri("https://itunes.apple.com/search?term=" + query + "&entity=song&limit=10")
                    .retrieve()
                    .body(String.class);

            Map<String, Object> data = objectMapper.readValue(
                    response,
                    new TypeReference<Map<String, Object>>() {}
            );

            return (List<Map<String, Object>>) data.get("results");

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}