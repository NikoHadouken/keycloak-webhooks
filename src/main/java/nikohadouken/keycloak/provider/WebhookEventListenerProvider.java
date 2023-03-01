package nikohadouken.keycloak.provider;

import java.util.List;
import java.util.Map;
import javax.json.*;
import org.jboss.logging.Logger;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import com.google.gson.Gson;

import nikohadouken.keycloak.webhook.WebhookClient;
import nikohadouken.keycloak.webhook.WebhookClientException;

public class WebhookEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private Logger logger;
    private Boolean showGroups;
    private Boolean showAttributes;

    private WebhookClient client;

    public WebhookEventListenerProvider(
            WebhookClient client,
            KeycloakSession session,
            Logger logger) {
        this.client = client;
        this.session = session;
        this.logger = logger;
        this.showGroups = true;
        this.showAttributes = true;
    }

    @Override
    public void onEvent(Event event) {
        logger.info("Event Occurred:" + toString(event));

        try {
            client.publish(toJson2(event));
        } catch (WebhookClientException e) {
            logger.error(e);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        logger.info("Admin Event Occurred:" + toString(adminEvent));
        try {
            client.publish(toJson2(adminEvent));
        } catch (WebhookClientException e) {
            logger.error(e);
        }
    }

    @Override
    public void close() {

    }

    private String toString(Event event) {

        StringBuilder sb = new StringBuilder();

        sb.append("type=");

        sb.append(event.getType());

        sb.append(", realmId=");

        sb.append(event.getRealmId());

        sb.append(", clientId=");

        sb.append(event.getClientId());

        sb.append(", userId=");

        sb.append(event.getUserId());

        sb.append(", ipAddress=");

        sb.append(event.getIpAddress());

        if (event.getError() != null) {

            sb.append(", error=");

            sb.append(event.getError());

        }

        if (event.getDetails() != null) {

            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {

                sb.append(", ");

                sb.append(e.getKey());

                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {

                    sb.append("=");

                    sb.append(e.getValue());

                } else {

                    sb.append("='");

                    sb.append(e.getValue());

                    sb.append("'");

                }

            }

        }

        return sb.toString();

    }

    private String toString(AdminEvent adminEvent) {

        StringBuilder sb = new StringBuilder();

        sb.append("operationType=");

        sb.append(adminEvent.getOperationType());

        sb.append(", realmId=");

        sb.append(adminEvent.getAuthDetails().getRealmId());

        sb.append(", clientId=");

        sb.append(adminEvent.getAuthDetails().getClientId());

        sb.append(", userId=");

        sb.append(adminEvent.getAuthDetails().getUserId());

        sb.append(", ipAddress=");

        sb.append(adminEvent.getAuthDetails().getIpAddress());

        sb.append(", resourcePath=");

        sb.append(adminEvent.getResourcePath());

        if (adminEvent.getError() != null) {

            sb.append(", error=");

            sb.append(adminEvent.getError());

        }

        return sb.toString();

    }

    private UserModel getUserModelById(String userId) {
        var realmModel = session.getContext().getRealm();
        return session.users().getUserById(realmModel, userId);
    }

    // Returns a JsonArrayBuilder suitable for inserting
    // into a JsonObjectBuilder later on.
    //
    private JsonArrayBuilder userGroups(UserModel user) {
        var jsonArray = Json.createArrayBuilder();

        user.getGroupsStream().forEach(grp -> jsonArray.add(grp.getName()));

        return jsonArray;
    }

    // Returns a JsonObjectBuilder suitable for inserting
    // into another JsonObjectBuilder later on.
    //
    private JsonObjectBuilder userAttributes(UserModel user) {
        JsonObjectBuilder jsonObj = Json.createObjectBuilder();
        Map<String, List<String>> attrs = user.getAttributes();

        for (Map.Entry<String, List<String>> e : attrs.entrySet()) {
            JsonArrayBuilder jsonArray = Json.createArrayBuilder();
            for (String value : e.getValue()) {
                if (value != null) {
                    jsonArray.add(value);
                }
            }

            jsonObj.add(e.getKey(), jsonArray);
        }

        return jsonObj;
    }

    private String toJson(Event event) {

        JsonObjectBuilder obj = Json.createObjectBuilder();

        if (event.getType() != null) {
            obj.add("type", event.getType().toString());
        }

        if (event.getRealmId() != null) {
            obj.add("realmId", event.getRealmId().toString());
        }

        if (event.getClientId() != null) {
            obj.add("clientId", event.getClientId().toString());
        }

        if (event.getUserId() != null) {
            String userId = event.getUserId().toString();
            obj.add("userId", userId);

            UserModel user = getUserModelById(userId);
            if (user != null) {

                if (showGroups) {
                    obj.add("userGroups", userGroups(user));
                }

                if (showAttributes) {
                    obj.add("userAttributes", userAttributes(user));
                }
            }
        }

        if (event.getIpAddress() != null) {
            obj.add("ipAddress", event.getIpAddress().toString());
        }

        if (event.getError() != null) {
            obj.add("error", event.getError().toString());
        }

        if (event.getDetails() != null) {
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                obj.add(e.getKey(), e.getValue().toString());
            }
        }

        return obj.build().toString();

    }

    private String toJson(AdminEvent adminEvent) {
        JsonObjectBuilder obj = Json.createObjectBuilder();

        obj.add("type", "ADMIN_EVENT");

        if (adminEvent.getOperationType() != null) {
            obj.add("operationType", adminEvent.getOperationType().toString());
        }

        if (adminEvent.getAuthDetails() != null) {
            if (adminEvent.getAuthDetails().getRealmId() != null) {
                obj.add("realmId", adminEvent.getAuthDetails().getRealmId().toString());
            }

            if (adminEvent.getAuthDetails().getClientId() != null) {
                obj.add("clientId", adminEvent.getAuthDetails().getClientId().toString());
            }

            if (adminEvent.getAuthDetails().getUserId() != null) {
                String userId = adminEvent.getAuthDetails().getUserId().toString();
                obj.add("userId", userId);

                UserModel user = getUserModelById(userId);
                if (user != null) {

                    if (showGroups) {
                        obj.add("userGroups", userGroups(user));
                    }

                    if (showAttributes) {
                        obj.add("userAttributes", userAttributes(user));
                    }
                }
            }

            if (adminEvent.getAuthDetails().getIpAddress() != null) {
                obj.add("ipAddress", adminEvent.getAuthDetails().getIpAddress().toString());
            }

        }

        if (adminEvent.getResourceType() != null) {
            obj.add("resourceType", adminEvent.getResourceType().toString());
        }

        if (adminEvent.getResourcePath() != null) {
            obj.add("resourcePath", adminEvent.getResourcePath().toString());
        }

        if (adminEvent.getError() != null) {
            obj.add("error", adminEvent.getError().toString());
        }

        return obj.build().toString();
    }

    String toJson2(Event event) {
        Gson gson = new Gson();
        return gson.toJson(event);
    }

    String toJson2(AdminEvent event) {
        Gson gson = new Gson();
        return gson.toJson(event);
    }
}