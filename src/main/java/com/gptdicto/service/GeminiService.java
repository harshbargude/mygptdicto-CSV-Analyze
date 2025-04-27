package com.gptdicto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService(){
        this.webClient = WebClient.create("https://generativelanguage.googleapis.com");
        this.objectMapper = new ObjectMapper();
    }

    @Value("${google.api.key}")
    private String googleApiKey;

    @Value("${google.gemini.model}")
    private String geminiModel;


    public String buildPrompt(String csvData, String question) {
        return "You are an AI agent analyzing CSV data. Here is the data:\n" +
                csvData +
                "\nQuestion: " + question +
                " \nalwase give a accurate answer and no python code and.. also give the answer like you are telling\n"+
                "\n\nWhen the question asks for a graph, plot, or chart, provide the response in this exact format: " +
                "[DATA: labels=[label1, label2, ...], values=[value1, value2, ...], xAxisLabel=\"X-axis description\", yAxisLabel=\"Y-axis description\"], " +
                "where labels and values are derived from the CSV data, and xAxisLabel and yAxisLabel are dynamically determined from the question. " +
                "For example, if the question is 'Plot sales by month', use: " +
                "[DATA: labels=[Jan, Feb, Mar], values=[10, 20, 30], xAxisLabel=\"Month\", yAxisLabel=\"Sales\"]. " +
                "For pie charts, use the same format but understand that xAxisLabel and yAxisLabel might not be directly used in the visualization. " +
                "Suggest pie charts for data that represents parts of a whole, like market share, percentage distributions, or proportional data. " +
                "Extract xAxisLabel from the 'by [X]' part of the question and yAxisLabel from the 'of [Y]' part, or use sensible defaults if unclear. " +
                "Otherwise, respond with plain text."+
                "\n dont add your thoughts";
    }

    public String buildRequestBody(String prompt) {
        return String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                prompt.replace("\"", "\\\"")
        );
    }

    public String callGeminiApi(String requestBody) throws Exception {
        Mono<String> responseMono = webClient.post()
                .uri("/v1beta/models/"+geminiModel+":generateContent?key={apiKey}", googleApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        String responseJson = responseMono.block();
        JsonNode rootNode = objectMapper.readTree(responseJson);
        return rootNode.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();
    }
    

}
