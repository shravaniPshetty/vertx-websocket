package org.acme.websocket;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class WebSocketRoutesTest {
    @TestHTTPResource("/chat/bob")
    URI userBob;

    @TestHTTPResource("/chat/amy")
    URI userAmy;

    @Test
    void chatTest() throws InterruptedException {
        CountDownLatch connectLatch = new CountDownLatch(2);
        CountDownLatch messageLatch = new CountDownLatch(2);

        Vertx vertx = Vertx.vertx();
        HttpClient client = vertx.createHttpClient();
        try {
            AtomicReference<WebSocket> bobWebSocketAtomicReference = new AtomicReference<>();
            client.webSocket(userBob.getPort(), userBob.getHost(), userBob.getPath()).onSuccess(webSocket -> {
                bobWebSocketAtomicReference.set(webSocket);
                connectLatch.countDown();
            });

            AtomicReference<WebSocket> amyWebSocketAtomicReference = new AtomicReference<>();
            client.webSocket(userAmy.getPort(), userAmy.getHost(), userAmy.getPath()).onSuccess(webSocket -> {
                amyWebSocketAtomicReference.set(webSocket);
                connectLatch.countDown();
            });

            connectLatch.await(5, TimeUnit.SECONDS);

            WebSocket bobWebSocket = bobWebSocketAtomicReference.get();
            bobWebSocket.handler(message -> {
                if (message.toString().toLowerCase().contains("hi bob")) {
                    messageLatch.countDown();
                }
            });

            WebSocket amyWebSocket = amyWebSocketAtomicReference.get();
            amyWebSocket.handler(message -> {
                if (message.toString().toLowerCase().contains("hi amy")) {
                    messageLatch.countDown();
                }
            });

            bobWebSocket.write(Buffer.buffer("Hi Amy"));
            amyWebSocket.write(Buffer.buffer("Hi Bob"));

            messageLatch.await(5, TimeUnit.SECONDS);
        } finally {
            if (client != null) {
                client.close();
            }

            if (vertx != null) {
                vertx.close();
            }
        }
    }
}
