package dev.isxander.deckapi.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.intellij.lang.annotations.Language;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;

public class JSTab implements WebSocket.Listener, Closeable {
    private WebSocket ws;
    private final Gson gson;

    private final AtomicInteger nextId = new AtomicInteger(0);
    private final Map<Integer, ResponseFuture<?>> pendingEvals = new HashMap<>();

    private final StringBuilder message = new StringBuilder();

    private JSTab() {
        this.gson = new Gson();
    }

    public static CompletableFuture<JSTab> open(String wsUrl, HttpClient httpClient) {
        JSTab tab = new JSTab();
        return httpClient.newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), tab)
                .thenApply(webSocket -> {
                    tab.ws = webSocket;
                    return tab;
                });
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        this.message.append(data);

        if (!last) {
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        String completeMessage = this.message.toString();
        this.message.setLength(0);

        CEFIncomingMessage decodedMessage = gson.fromJson(completeMessage, CEFIncomingMessage.class);

        ResponseFuture<?> responseFuture = pendingEvals.remove(decodedMessage.id());
        if (responseFuture != null) {
            responseFuture.complete(decodedMessage.result(), gson);
        } else {
            System.out.println("Received message with no pending eval: " + data);
        }

        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + statusCode + " " + reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    public <T> CompletableFuture<T> eval(
            @Language(value = "JavaScript", prefix = JSContext.JS_CONTEXT)
            String javascript,
            Class<T> type,
            Object... args
    ) {
        int id = nextId.getAndIncrement();
        var message = new CEFOutgoingMessage(
                id,
                "Runtime.evaluate",
                Map.of(
                        "expression", javascript.stripIndent().replace("$$", "%s").formatted(args),
                        "userGesture", true
                )
        );

        ws.request(1);

        String json = gson.toJson(message);
        return ws.sendText(json, true)
                .thenCompose(ws1 -> {
                    CompletableFuture<T> f = new CompletableFuture<>();
                    pendingEvals.put(id, new ResponseFuture<>(f, type));
                    return f;
                });
    }

    @Override
    public void close() {
        ws.sendClose(WebSocket.NORMAL_CLOSURE, "Closing").join();
    }

    private record ResponseFuture<T>(CompletableFuture<T> future, Type type) {
        public void complete(JsonElement response, Gson gson) {
            try {
                T result = gson.fromJson(response, type);
                future.complete(result);
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }

        }
    }
}
