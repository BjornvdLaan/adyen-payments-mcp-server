package nl.bjornvanderlaan.adyenpayments.mcpserver;

import com.adyen.model.checkout.PaymentMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AdyenMcpServer {
    private static final Logger log = LoggerFactory.getLogger(AdyenMcpServer.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static AdyenPaymentService service;

    public static void main(String[] args) {
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        service = new AdyenPaymentService(AppConfig.get());

        McpSyncServer syncServer =  McpServer.sync(transportProvider)
                .serverInfo("adyen-payments-mcp-server", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true).logging().build()
                )
                .tools(getPaymentMethodsToolSpecification(), getIcedCoffeeToolSpecification())
                .build();

        log.info("Adyen Mcp Server started...");
    }

    private static McpServerFeatures.SyncToolSpecification getPaymentMethodsToolSpecification() {
        ObjectNode inputSchema;
        try {
            inputSchema = (ObjectNode) mapper.readTree("""
            {
              "type": "object",
              "properties": {
                "merchantAccount": {
                  "type": "string",
                  "description": "The merchant account identifier in Adyen (e.g., 'MyMerchant123')."
                },
                "countryCode": {
                  "type": "string",
                  "description": "The 2-letter country code (e.g., 'NL' for the Netherlands)."
                }
              },
              "required": ["merchantAccount", "countryCode"]
            }
        """);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse inputSchema JSON", e);
        }

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_payment_methods",
                        "Get all payment methods that are enabled for a specific merchant account, country code and currency",
                        String.valueOf(inputSchema)
                ),
                (exchange, arguments) -> {
                    List<PaymentMethod> paymentMethods;
                    try {
                        String merchant = (String) arguments.get("merchantAccount");
                        String country = (String) arguments.get("countryCode");
                        paymentMethods = service.getPaymentMethods(merchant, country);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent("Cannot connect to Adyen because: " + e.getMessage())),
                                true);
                    }

                    List<McpSchema.Content> mcpContents = paymentMethods.stream()
                            .<McpSchema.Content>map(pm -> new McpSchema.TextContent(pm.getName()))
                            .toList();
                    return new McpSchema.CallToolResult(mcpContents, false);
                }
        );
    }

    private static McpServerFeatures.SyncToolSpecification getIcedCoffeeToolSpecification() {
        String inputSchema = """
        {
          "type": "object",
          "properties": {}
        }
        """;

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(
                        "get_iced_coffee",
                        "Fetch the available iced coffees",
                        inputSchema
                ),
                (exchange, arguments) -> {
                    try (CloseableHttpClient client = HttpClients.createDefault()) {
                        BasicClassicHttpRequest request = new BasicClassicHttpRequest(
                                "GET", "https://api.sampleapis.com/coffee/iced");

                        String responseBody = client.execute(request, (ClassicHttpResponse response) -> {
                            int status = response.getCode();
                            if (status >= 200 && status < 300) {
                                return EntityUtils.toString(response.getEntity());
                            } else {
                                throw new IOException("Coffee machine shows code " + status);
                            }
                        });

                        JsonNode jsonData = mapper.readTree(responseBody);
                        if (!jsonData.isArray()) {
                            return new McpSchema.CallToolResult(
                                    List.of(new McpSchema.TextContent("No coffee is served today.")),
                                    true
                            );
                        }

                        List<McpSchema.Content> coffeeList = new ArrayList<>();
                        for (JsonNode item : jsonData) {
                            String title = item.has("title") ? item.get("title").asText() : "Unknown coffee";
                            coffeeList.add(new McpSchema.TextContent(title));
                        }

                        return new McpSchema.CallToolResult(coffeeList, false);
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent("The barista is busy and says: " + e.getMessage())),
                                true);
                    }
                }
        );
    }
}