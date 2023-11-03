package org.acme.websocket;

import io.vertx.core.http.ServerWebSocket;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.vertx.websocket.VertxWebsocketConstants;
import org.jboss.logging.Logger;

@ApplicationScoped
public class WebSocketRoutes extends RouteBuilder {
    static final Logger LOG = Logger.getLogger(WebSocketRoutes.class);

    @Inject
    SessionManager sessionManager;

    @Override
    public void configure() throws Exception {
        from("vertx-websocket:/chat/{userName}?fireWebSocketConnectionEvents=true")
                .choice()

                // Capture OPEN events to track newly connected peers
                .when(simple("${header.CamelVertxWebsocket.event} == 'OPEN'"))
                .process(exchange -> {
                    Message message = exchange.getMessage();
                    String userName = message.getHeader("userName", String.class);
                    if (!sessionManager.isSessionExists(userName)) {
                        String connectionKey = message.getHeader(VertxWebsocketConstants.CONNECTION_KEY, String.class);
                        sessionManager.startSession(userName, connectionKey);
                        LOG.infof("Session started for user: %s", userName);
                        message.setBody("<<<<< " + userName + ": joined the chat");
                    } else {
                        // Reject connections for a user name that is already taken
                        ServerWebSocket webSocket = message.getBody(ServerWebSocket.class);
                        // RFC 6455 status codes: https://datatracker.ietf.org/doc/html/rfc6455#section-7.4.1
                        webSocket.close((short) 1000, "SESSION_ALREADY_EXISTS");
                        LOG.warnf("Rejected connection for user: %s. User session already exists", userName);
                    }
                })
                .to("vertx-websocket:/chat/{userName}?sendToAll=true")
                .endChoice()

                // Capture MESSAGE events and broadcast them to all connected peers
                .when(simple("${header.CamelVertxWebsocket.event} == 'MESSAGE'"))
                .log("New message from user ${header.userName}: ${body}")
                .setBody().simple("<<<<< ${header.userName}: ${body}")
                .to("vertx-websocket:/chat/{userName}?sendToAll=true")
                .endChoice()

                // Capture CLOSE events to track peers disconnecting
                .when(simple("${header.CamelVertxWebsocket.event} == 'CLOSE'"))
                .process(exchange -> {
                    Message message = exchange.getMessage();
                    String userName = message.getHeader("userName", String.class);
                    String connectionKey = message.getHeader(VertxWebsocketConstants.CONNECTION_KEY, String.class);
                    String userConnectionKey = sessionManager.getConnectionKey(userName);
                    if (!connectionKey.equals(userConnectionKey)) {
                        // WebSocket was closed due to a username that is already active. No need for further processing
                        message.setBody(null);
                        return;
                    }

                    if (sessionManager.isSessionExists(userName)) {
                        LOG.infof("Session ended for user: %s", userName);
                        sessionManager.endSession(userName);
                    }
                })
                .setBody().simple("<<<<< ${header.userName} left the chat")
                .to("vertx-websocket:/chat/{userName}?sendToAll=true")
                .endChoice();
    }
}
