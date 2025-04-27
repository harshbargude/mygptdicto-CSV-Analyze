package com.gptdicto.service;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MlServiceImpl implements MlService{

    @Override
    public String trainModel(String csvContent, String targetColumn) {
            try {
            // Create a temporary file with the CSV content
            Path tempFile = Files.createTempFile("upload-", ".csv");
            Files.writeString(tempFile, csvContent);

            // Create multipart request to Python service
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()))
                    .filename("data.csv");

            if (targetColumn != null && !targetColumn.isEmpty()) {
                bodyBuilder.part("target_column", targetColumn);
            }

            // Call the Python microservice
            String result = WebClient.create("http://localhost:8000")
                    .post()
                    .uri("/train_model/")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Clean up temp file
            Files.deleteIfExists(tempFile);

            return result;
        } catch (Exception e) {
            return "Error training model: " + e.getMessage();
        }
    }

    @Override
    public String getPredictions(String csvContent, String modelId) {
        try {
            // Create a temporary file with the CSV content
            Path tempFile = Files.createTempFile("upload-", ".csv");
            Files.writeString(tempFile, csvContent);

            // Create multipart request to Python service
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()))
                    .filename("data.csv");

            // Call the Python microservice
            String result = WebClient.create("http://localhost:8000")
                    .post()
                    .uri("/predict/" + modelId)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Clean up temp file
            Files.deleteIfExists(tempFile);

            // Format the JSON response for better readability
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(result);
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            } catch (Exception e) {
                // If not valid JSON or other error, return as is
                return result;
            }
        } catch (Exception e) {
            return "Error calling ML service: " + e.getMessage();
        }
    }

}
