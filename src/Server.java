import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.Executors;

public class Server {

    public static void main(String[] args) throws Exception {

        IdempotencyStore store = new MySqlIdempotencyStore();
        IdempotencyService service = new IdempotencyService(store);

        HttpServer server =
                HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/process", exchange -> {

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Origin", "*");

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Headers", "Content-Type, Idempotency-Key");

            exchange.getResponseHeaders()
                    .set("Access-Control-Allow-Methods", "POST, OPTIONS");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String idemKey =
                    exchange.getRequestHeaders()
                            .getFirst("Idempotency-Key");

            if (idemKey == null || idemKey.isBlank()) {
                sendResponse(exchange, 400, "Missing Idempotency-Key");
                return;
            }

            String requestBody =
                    new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            try {

                IdempotencyService.Result result =
                        service.process(idemKey, requestBody);

                sendResponse(
                        exchange,
                        result.statusCode(),
                        result.responseBody()
                );

            } catch (SQLException e) {

                e.printStackTrace();

                sendResponse(
                        exchange,
                        500,
                        "Database error"
                );
            }
        });

        server.setExecutor(
                Executors.newFixedThreadPool(10)
        );

        server.start();

        System.out.println(
                "Server started on http://localhost:8080"
        );
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String body
    ) throws IOException {

        byte[] responseBytes =
                body.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders()
                .set("Content-Type", "application/json");

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(responseBytes);
        }
    }
}