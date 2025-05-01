package nl.bjornvanderlaan.adyenpayments.mcpserver;

import com.adyen.model.checkout.PaymentMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class AdyenMcpServer {
    private static final Logger log = LoggerFactory.getLogger(AdyenMcpServer.class);
    private static AdyenPaymentService service;

    public static void main(String[] args) {
        StdioServerTransportProvider transportProvider = new StdioServerTransportProvider(new ObjectMapper());
        service = new AdyenPaymentService(AppConfig.get());
        McpServerFeatures.SyncToolSpecification paymentMethodsToolSpecification = getPaymentMethodsToolSpecification();

        McpSyncServer syncServer =  McpServer.sync(transportProvider)
                .serverInfo("adyen-payments-mcp-server", "0.0.1")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true).logging().build()
                )
                .tools(paymentMethodsToolSpecification)
                .build();

        log.info("Adyen Mcp Server started...");
    }

    private static McpServerFeatures.SyncToolSpecification getPaymentMethodsToolSpecification() {
        String schema = """
                {
                  "name": "get_payment_methods",
                  "description": "Fetch available payment methods from Adyen for a given merchant account and country code.",
                  "parameters": {
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
                }
                """;

        return new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool("get_payment_methods", "Get all payment methods that are enabled for a specific merchant account, country code and currency", schema),
                (McpSyncServerExchange exchange, Map<String, Object> arguments) -> {
                    List<PaymentMethod> paymentMethods;
                    try {
                        paymentMethods = service.getPaymentMethods("merchant", "NL");
                    } catch (Exception e) {
                        return new McpSchema.CallToolResult(List.of(), true);
                    }

                    List<McpSchema.Content> mcpContents = paymentMethods.stream()
                            .<McpSchema.Content>map(pm -> new McpSchema.TextContent(pm.getName())).toList();
                    return new McpSchema.CallToolResult(mcpContents, false);
                }
        );
    }
}