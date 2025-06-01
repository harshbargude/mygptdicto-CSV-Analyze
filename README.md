# MyGPTDicto (MY GEMINI Dicto*)

**MyGPTDicto** is a Spring Boot application that leverages the LangChain and Gemini Pro API to read CSV files and answer questions based on their content. It’s designed to provide an intelligent, conversational way to query data stored in CSV format.

## Features
- Upload and process CSV files
- Ask natural language questions about the CSV data
- Get accurate, AI-powered responses using the LangChain and Gemini Pro API

## Prerequisites
Before running the project, ensure you have:
- **Java 17** or higher installed
- **Maven** for dependency management
- A valid **Gemini Pro API key** (obtain it from [their official site](https://ai.google.dev/gemini-api/docs/api-key))

## Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/harshbargude/mygptdicto.git
2. **Navigate to the project directory**:
   ```bash
   cd mygptdicto
3. **Configure the API key**:
  Open src/main/resources/application.properties and add your Gemini Pro API key:
    ```bash
    google.api.key=YOUR_API_KEY_HERE
4. **Build the project**:
    ```bash
    mvn clean install
5. **Run the application**:
    ```bash
    mvn spring-boot:run

## API Endpoints

### AiController

Manages CSV uploads, AI-based question answering, and session handling.

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | / | Displays the CSV file upload form for data analysis |
| POST | /upload | Uploads a CSV file and a question, processes it with AI, and returns the analysis result. Optionally includes a generated graph if detected in the response |
| POST | /ask | Submits a new question for the previously uploaded CSV file, processes it with AI, and returns the result. Requires a CSV file in session. Optionally includes a generated graph |
| GET | /result | Displays the result page for the latest AI analysis or question response |
| GET | /predict | Displays the page for making predictions using a trained ML model |
| GET | /reset | Clears the CSV content from the session and redirects to the upload page |

### CleanAndProcessController

Handles CSV data analysis and cleaning via an external Python API.

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | /clean-process | Displays the page for uploading a CSV file for data cleaning and analysis |
| POST | /upload-csv | Uploads a CSV file, sends it to a Python API for analysis, and returns dataset information, missing values, data types, statistics, duplicates, and data quality suggestions |
| POST | /clean_data | Uploads a CSV file with cleaning parameters (e.g., fill value, outlier threshold, null threshold), sends it to a Python API for cleaning, and returns the cleaned CSV file as a downloadable resource |

### ModelTrainController

Facilitates machine learning model training and predictions.

| Method | Endpoint | Description |
| --- | --- | --- |
| GET | /train | Displays the page for uploading a CSV file to train a machine learning model |
| POST | /train-model | Uploads a CSV file and target column, trains an ML model via the ML service, stores the model ID in the session, and returns the training result |
| POST | /predict | Uploads a CSV file, uses a trained model (specified by model ID or from session) to make predictions via the ML service, and returns the prediction results |


## **Technologies Used**
- Spring Boot: Backend framework for building the application
- Gemini Pro API: For natural language processing and answering questions
- Java: Core programming language
- Maven: Dependency management

### Contact
For questions or feedback, reach out to me at harshbargude03@gmail.com or connect on [@HBargude](https://x.com/HBargude).

