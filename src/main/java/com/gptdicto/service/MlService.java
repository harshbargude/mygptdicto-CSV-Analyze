package com.gptdicto.service;

public interface MlService {

    String trainModel(String csvContent, String targetColumn);


    /**
     * Sends a CSV file to the Python ML microservice for prediction
     *
     * @param csvContent The CSV data to analyze
     * @return The prediction results from the ML model
     */
    String getPredictions(String csvContent, String modelId);
    
}
