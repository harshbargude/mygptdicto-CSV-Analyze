package com.gptdicto.service;

public interface AiService {
    /**
     * Processes a CSV content and a question, sending it to the AI model for analysis.
     *
     * @param csvContent The CSV data to analyze
     * @param question The question to ask about the data
     * @return The AI response, possibly including chart generation information
     */
    String askAgent(String csvContent, String question);

    /**
     * Extracts CSV content from a code block if present
     *
     * @param content Potential code block containing CSV
     * @return Extracted CSV content
     */
    String extractCsvFromCodeBlock(String content);




}