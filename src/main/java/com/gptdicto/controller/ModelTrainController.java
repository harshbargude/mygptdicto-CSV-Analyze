package com.gptdicto.controller;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gptdicto.model.CsvRequest;
// import com.gptdicto.service.AiServiceImpl;
import com.gptdicto.service.MlService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ModelTrainController {

    @Autowired
    private MlService mlService;

    @GetMapping("/train")
    public String showTrainModelPage(Model model) {
        model.addAttribute("csvRequest", new CsvRequest());
        return "train-model";
    }

    
    @PostMapping("/train-model")
    public String trainModel(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "targetColumn", required = false) String targetColumn,
                             HttpSession session,
                             Model model) {
        try {
            String csvContent = new String(file.getBytes());
            session.setAttribute("csvContent", csvContent);
            String limitedCsvContent = limitCsvContent(csvContent, 5);

            // Call the ML service to train a model
            String trainingResult = mlService.trainModel(csvContent, targetColumn);

            // Parse the model ID from the result
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode resultNode = objectMapper.readTree(trainingResult);
            String modelId = resultNode.path("model_id").asText();

            // Store model ID in session
            session.setAttribute("modelId", modelId);

            CsvRequest csvRequest = new CsvRequest();
            csvRequest.setCsvContent(limitedCsvContent);
            csvRequest.setResponse("Model trained successfully.\nModel ID: " + modelId);
            model.addAttribute("csvRequest", csvRequest);
            model.addAttribute("modelId", modelId);

            return "model-trained";
        } catch (Exception e) {
            model.addAttribute("error", "Error training model: " + e.getMessage());
            return "upload";
        }
    }


    @PostMapping("/predict")
    public String getPredictions(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "modelId", required = false) String modelId,
                                 HttpSession session,
                                 Model model) {
        try {
            // If modelId is not provided, use the one from session
            if (modelId == null || modelId.isEmpty()) {
                modelId = (String) session.getAttribute("modelId");
                if (modelId == null) {
                    model.addAttribute("error", "No model ID provided. Please train a model first.");
                    return "upload";
                }
            }

            String csvContent = new String(file.getBytes());
            session.setAttribute("csvContent", csvContent);
            String limitedCsvContent = limitCsvContent(csvContent, 5);

            // Call the ML service
            String predictions = mlService.getPredictions(csvContent, modelId);

            CsvRequest csvRequest = new CsvRequest();
            csvRequest.setCsvContent(limitedCsvContent);
            csvRequest.setResponse("ML Model Predictions:\n" + predictions);
            model.addAttribute("csvRequest", csvRequest);
            model.addAttribute("modelId", modelId);

            return "prediction-result";
        } catch (Exception e) {
            model.addAttribute("error", "Error processing file for prediction: " + e.getMessage());
            return "upload";
        }
    }

    private String limitCsvContent(String csvContent, int maxLines) {
            String[] lines = csvContent.split("\n");
            if (lines.length <= maxLines) {
                return csvContent; // Return full content if under maxLines
            }
            StringBuilder limitedContent = new StringBuilder();
            for (int i = 0; i < maxLines; i++) {
                limitedContent.append(lines[i]).append("\n");
            }
            limitedContent.append("... (showing first ").append(maxLines).append(" lines of ").append(lines.length).append(" total)");
            return limitedContent.toString();
        }


}
