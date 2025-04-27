package com.gptdicto.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

    private static class GraphData {
        String[] labels;
        double[] values;
        String xAxisLabel;
        String yAxisLabel;

        public GraphData(String[] labels, double[] values, String xAxisLabel, String yAxisLabel) {
            this.labels = labels;
            this.values = values;
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
        }
    }

    public boolean isGraphRequest(String question) {
        return question.toLowerCase().contains("graph") ||
                question.toLowerCase().contains("plot") ||
                question.toLowerCase().contains("chart");
    }

    public boolean isPieChartRequest(String question) {
        return question.toLowerCase().contains("pie chart") ||
                question.toLowerCase().contains("piechart") ||
                question.toLowerCase().contains("pie graph");
    }

    public String processGraphRequest(String responseText, String question, boolean isPieChart) {
        try {
            // Extract data from response
            GraphData graphData = extractGraphData(responseText);

            if (graphData != null) {
                // Generate appropriate chart
                String graphFilename = generateChart(graphData, question, isPieChart);
                return responseText + "\n[Graph generated and available below:" + graphFilename + "]";
            }

            return responseText;
        } catch (Exception e) {
            return responseText + "\n[Graph generation failed: " + e.getMessage() + "]";
        }
    }

    public GraphData extractGraphData(String responseText) {
        // First check for data in code blocks
        Pattern codeBlockPattern = Pattern.compile("```[\\s\\S]*?\\[DATA: labels=\\[(.*?)\\], values=\\[(.*?)\\], xAxisLabel=\"(.*?)\", yAxisLabel=\"(.*?)\"\\][\\s\\S]*?```", Pattern.DOTALL);
        Matcher codeBlockMatcher = codeBlockPattern.matcher(responseText);

        if (codeBlockMatcher.find()) {
            return parseGraphData(
                    codeBlockMatcher.group(1),
                    codeBlockMatcher.group(2),
                    codeBlockMatcher.group(3),
                    codeBlockMatcher.group(4)
            );
        }

        // If not found in code block, try regular pattern
        Pattern dataPattern = Pattern.compile(
                "\\[?DATA: labels=\\[(.*?)\\], values=\\[(.*?)\\], xAxisLabel=\"(.*?)\", yAxisLabel=\"(.*?)\"\\]?");
        Matcher dataMatcher = dataPattern.matcher(responseText);

        if (dataMatcher.find()) {
            return parseGraphData(
                    dataMatcher.group(1),
                    dataMatcher.group(2),
                    dataMatcher.group(3),
                    dataMatcher.group(4)
            );
        }

        return null;
    }

    public GraphData parseGraphData(String labelsStr, String valuesStr, String xAxisLabel, String yAxisLabel) {
        // Clean and parse labels
        String[] labelsRaw = labelsStr.split(",");
        String[] labels = new String[labelsRaw.length];
        for (int i = 0; i < labelsRaw.length; i++) {
            labels[i] = labelsRaw[i].trim().replace("'", "").replace("\"", "");
        }

        // Clean and parse values
        String[] valuesRaw = valuesStr.split(",");
        double[] values = new double[valuesRaw.length];
        for (int i = 0; i < valuesRaw.length; i++) {
            values[i] = Double.parseDouble(valuesRaw[i].trim().replace("'", "").replace("\"", ""));
        }

        return new GraphData(labels, values, xAxisLabel, yAxisLabel);
    }

    public String generateChart(GraphData data, String question, boolean isPieChart) throws Exception {
        String graphTitle = extractGraphTitle(question);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String graphFilename = "graph-" + timestamp + ".png";
        Path tempImagePath = Files.createTempFile("graph-", ".png");
        Path finalImagePath = Paths.get("./target/classes/static/" + graphFilename);

        JFreeChart chart;

        if (isPieChart) {
            // Create dataset for pie chart
            DefaultPieDataset<String> pieDataset = new DefaultPieDataset<>();
            for (int i = 0; i < data.labels.length; i++) {
                pieDataset.setValue(data.labels[i], data.values[i]);
            }

            // Create pie chart
            chart = ChartFactory.createPieChart(
                    graphTitle,         // Chart title
                    pieDataset,         // Dataset
                    true,               // Include legend
                    true,               // Include tooltips
                    false               // No URLs
            );

            // Save pie chart with square dimensions
            ChartUtils.saveChartAsPNG(tempImagePath.toFile(), chart, 800, 600);
        } else {
            // Create dataset for bar chart
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int i = 0; i < data.labels.length; i++) {
                dataset.addValue(data.values[i], data.yAxisLabel, data.labels[i]);
            }

            // Create bar chart
            chart = ChartFactory.createBarChart(
                    graphTitle,
                    data.xAxisLabel,
                    data.yAxisLabel,
                    dataset
            );

            // Save bar chart
            ChartUtils.saveChartAsPNG(tempImagePath.toFile(), chart, 800, 400);
        }

        // Move chart to final location
        Files.createDirectories(finalImagePath.getParent());
        Files.move(tempImagePath, finalImagePath);

        return graphFilename;
    }

    private String extractGraphTitle(String question) {
        // Simple extraction: look for "of X by Y" pattern
        Pattern pattern = Pattern.compile("of\\s+(.+?)\\s+by\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(question);
        if (matcher.find()) {
            return capitalize(matcher.group(1)) + " by " + capitalize(matcher.group(2));
        }

        // Try other patterns
        if (question.toLowerCase().contains("show") || question.toLowerCase().contains("display")) {
            Pattern showPattern = Pattern.compile("(show|display)\\s+(.+?)\\s+(for|of|by|in)", Pattern.CASE_INSENSITIVE);
            Matcher showMatcher = showPattern.matcher(question);
            if (showMatcher.find()) {
                return capitalize(showMatcher.group(2));
            }
        }

        return "Data Visualization"; // Fallback
    }

    public String capitalize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

}
