package com.gptdicto.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CleanAndProcessController {

    private static final Logger logger = LoggerFactory.getLogger(CleanAndProcessController.class);

    private final RestTemplate restTemplate;
    private final String PYTHON_API_URL = "http://127.0.0.1:8000";
    private final String PYTHON_CSV_INFO_URI = "/analyze_data";

    public CleanAndProcessController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/clean-process")
    public String getCleanAndProcesPage() {
        return "clean-process";
    }

    @PostMapping("/upload-csv")
    public String analyzeData(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            logger.info("Sending request to: " + PYTHON_API_URL + PYTHON_CSV_INFO_URI);
            logger.info("File name: " + file.getOriginalFilename());
            logger.info("File size: " + file.getSize() + " bytes");

            ResponseEntity<Map> response = restTemplate.exchange(
                    PYTHON_API_URL + PYTHON_CSV_INFO_URI,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    model.addAttribute("datasetInfo", responseBody.get("dataset_info"));
                    model.addAttribute("missingValues", responseBody.get("missing_values"));
                    model.addAttribute("dataTypes", responseBody.get("data_types"));
                    model.addAttribute("numericStatistics", responseBody.get("numeric_statistics"));
                    model.addAttribute("categoricalStatistics", responseBody.get("categorical_statistics"));
                    model.addAttribute("duplicateInfo", responseBody.get("duplicate_info"));
                    model.addAttribute("dataQualitySuggestions", responseBody.get("data_quality_suggestions"));
                } else {
                    model.addAttribute("error", "Empty response received from the analysis server");
                }
            } else {
                model.addAttribute("error", "No response received from the analysis server");
            }

            return "csv-data-info-02";
        } catch (Exception e) {
            logger.error("Error analyzing data: ", e);
            model.addAttribute("error",
                    "An unexpected error occurred while analyzing the data. Please try again later.");
            return "dashboard";
        }
    }

    @PostMapping("/clean_data")
    public ResponseEntity<ByteArrayResource> cleanData(
            @RequestParam("file") MultipartFile file,
            @RequestParam Map<String, String> params) throws IOException {
        if (!"text/csv".equals(file.getContentType())) {
            return ResponseEntity.badRequest()
                    .body(new ByteArrayResource("Invalid file format. Please upload a CSV file.".getBytes()));
        }
        System.out.println("In clean data");
        try {
            // Parse fill_value with a default value of 0.0
            String fillValueStr = params.getOrDefault("fill_value", "0.0");
            double fillValue = 0.0;
            if (!fillValueStr.isEmpty()) {
                fillValue = Double.parseDouble(fillValueStr);
            }
        
            // Parse outlier_threshold with a default value of 1.5
            String outlierThresholdStr = params.getOrDefault("outlier_threshold", "1.5");
            double outlierThreshold = 1.5;
            if (!outlierThresholdStr.isEmpty()) {
                outlierThreshold = Double.parseDouble(outlierThresholdStr);
            }
        
            // Parse null_threshold with a default value of 1.0
            String nullThresholdStr = params.getOrDefault("null_threshold", "1.0");
            double nullThreshold = 1.0;
            if (!nullThresholdStr.isEmpty()) {
                nullThreshold = Double.parseDouble(nullThresholdStr);
            }
        
            // Validate null_threshold range
            if (nullThreshold < 0 || nullThreshold > 1) {
                return ResponseEntity.badRequest()
                        .body(new ByteArrayResource("Invalid null_threshold. Must be between 0 and 1.".getBytes()));
            }
        } catch (NumberFormatException e) {
            // Log the specific parameter causing the error for debugging
            System.err.println("Error parsing numeric parameter: " + e.getMessage());
            return ResponseEntity.badRequest().body(new ByteArrayResource("Invalid numeric parameter.".getBytes()));
        }
        logger.info("Hi");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        params.forEach(body::add);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                PYTHON_API_URL + "/clean_data",
                HttpMethod.POST,
                requestEntity,
                byte[].class);
        logger.info("Hi after requestEntity");
        System.out.println("hello");
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=cleaned_" + file.getOriginalFilename())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new ByteArrayResource(response.getBody()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ByteArrayResource("Error processing the file".getBytes()));
        }
    }
}