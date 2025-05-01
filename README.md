# MCP Server for Adyen Payments Integration

This project provides a simple MCP server for models to interact with Adyen's Advanced Flow API.

NOTE: this project is a work in progress. It is not ready for any actual use.

## Prerequisites

* ...

## Setup Instructions

1.  **Clone the repository**

2.  **Update Configuration:**

    * **Set the API Key:**
      For local testing, the API key for your target HTTP API should be set as an environment variable named `ADYEN-API-KEY`.
      On Linux/macOS:
        ```bash
        export ADYEN-API-KEY="your_secret_api_key"
        ```
      On Windows (Command Prompt):
        ```bash
        set ADYEN-API-KEY="your_secret_api_key"
        ```
      On Windows (PowerShell):
        ```powershell
        $env:ADYEN-API-KEY = "your_secret_api_key"
        ```

3.  **Build the Project:**
    Navigate to the root directory of the `mcp-server` project in your terminal and run the following Maven command:
    ```bash
    ./gradlew clean jar
    ```
    This will create a JAR file (likely in the `target` directory).

4.  **Run the MCP Server:**
    After the build is successful, run the JAR file using Java:
    ```bash
    java -jar target/mcp-server-1.0.0.jar
    ```
    (The exact JAR file name might vary slightly depending on the Gradle version).

    You should see the output: `MCP Server started on port 8080`.

## Using the MCP Server

Once the MCP server is running, AI models can use the tools.
If you just want to test this tool out then the MCP Inspector is a great option:
```
npx @modelcontextprotocol/inspector
```