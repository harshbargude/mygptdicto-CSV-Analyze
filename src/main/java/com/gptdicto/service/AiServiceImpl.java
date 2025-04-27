package com.gptdicto.service;

import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiServiceImpl implements AiService {

    private final GeminiService geminiService;
    private final GraphService graphService;

    // change(1)
    public AiServiceImpl(GeminiService geminiService, GraphService graphService){
        this.geminiService = geminiService;
        this.graphService = graphService;
    }

    @Value("${google.api.key}")
    private String googleApiKey;

    // private final WebClient webClient;
    // private final ObjectMapper objectMapper;

    @Override
    public String askAgent(String csvContent, String question) {
        // First, check if the csvContent is wrapped in code blocks and extract it
        // String processedCsvContent = extractCsvFromCodeBlock(csvContent);

        StringBuilder parsedData = new StringBuilder();
        try (CSVReader csvReader = new CSVReader(new StringReader(csvContent))) {
            List<String[]> rows = csvReader.readAll();
            for (String[] row : rows) {
                parsedData.append(String.join(", ", row)).append("\n");
            }
        } catch (Exception e) {
            return "Error parsing CSV: " + e.getMessage();
        }

        String prompt = geminiService.buildPrompt(parsedData.toString(), question);
        String requestBody = geminiService.buildRequestBody(prompt);

        try {
            String responseText = geminiService.callGeminiApi(requestBody);

            // Normalize line breaks
            responseText = responseText.replace("\r\n", "\n").replace("\r", "\n");

            // Check if this is a graph request
            boolean isGraphRequest = graphService.isGraphRequest(question);
            boolean isPieChartRequest = graphService.isPieChartRequest(question);

            if (isGraphRequest && responseText != null) {
                // Process graph data and generate chart
                return graphService.processGraphRequest(responseText, question, isPieChartRequest);
            }

            // Return normalized text for non-graph requests
            return responseText != null ? responseText : "No response from Gemini API";
        } catch (Exception e) {
            return "Error calling Gemini API or generating graph: " + e.getMessage();
        }
    }

    @Override
    public String extractCsvFromCodeBlock(String content) {
        if (content == null) return "";

        // Pattern to match content inside triple backticks
        Pattern pattern = Pattern.compile("```(?:csv)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return content; // Return original content if no code block found
    }

    // @Override
    // public String trainModel(String csvContent, String targetColumn) {
    //     try {
    //         // Create a temporary file with the CSV content
    //         Path tempFile = Files.createTempFile("upload-", ".csv");
    //         Files.writeString(tempFile, csvContent);

    //         // Create multipart request to Python service
    //         MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
    //         bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()))
    //                 .filename("data.csv");

    //         if (targetColumn != null && !targetColumn.isEmpty()) {
    //             bodyBuilder.part("target_column", targetColumn);
    //         }

    //         // Call the Python microservice
    //         String result = WebClient.create("http://localhost:8000")
    //                 .post()
    //                 .uri("/train_model/")
    //                 .contentType(MediaType.MULTIPART_FORM_DATA)
    //                 .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
    //                 .retrieve()
    //                 .bodyToMono(String.class)
    //                 .block();

    //         // Clean up temp file
    //         Files.deleteIfExists(tempFile);

    //         return result;
    //     } catch (Exception e) {
    //         return "Error training model: " + e.getMessage();
    //     }
    // }

    // @Override
    // public String getPredictions(String csvContent, String modelId) {
    //     try {
    //         // Create a temporary file with the CSV content
    //         Path tempFile = Files.createTempFile("upload-", ".csv");
    //         Files.writeString(tempFile, csvContent);

    //         // Create multipart request to Python service
    //         MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
    //         bodyBuilder.part("file", new FileSystemResource(tempFile.toFile()))
    //                 .filename("data.csv");

    //         // Call the Python microservice
    //         String result = WebClient.create("http://localhost:8000")
    //                 .post()
    //                 .uri("/predict/" + modelId)
    //                 .contentType(MediaType.MULTIPART_FORM_DATA)
    //                 .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
    //                 .retrieve()
    //                 .bodyToMono(String.class)
    //                 .block();

    //         // Clean up temp file
    //         Files.deleteIfExists(tempFile);

    //         // Format the JSON response for better readability
    //         try {
    //             ObjectMapper mapper = new ObjectMapper();
    //             JsonNode jsonNode = mapper.readTree(result);
    //             return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    //         } catch (Exception e) {
    //             // If not valid JSON or other error, return as is
    //             return result;
    //         }
    //     } catch (Exception e) {
    //         return "Error calling ML service: " + e.getMessage();
    //     }
    // }

}