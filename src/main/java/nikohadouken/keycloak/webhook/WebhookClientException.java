package nikohadouken.keycloak.webhook;

public class WebhookClientException extends RuntimeException {
    public WebhookClientException(String errorMessage) {
        super(errorMessage);
    }

    public WebhookClientException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}