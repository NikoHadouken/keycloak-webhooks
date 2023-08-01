package nikohadouken.keycloak.webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class WebhookClient {
    private String baseUrl;
    private String apiKey;
    private HttpClient client;

    public WebhookClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        client = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void publish(String payload) throws WebhookClientException {
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(baseUrl))
                .headers("Content-Type", "application/json")
                .headers("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Throwable err) {
            throw new WebhookClientException("webhook publish failed", err);
        }

        System.out.println(response.statusCode());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new WebhookClientException("webhook responded with code: " + response.statusCode());
        }
    }
}
