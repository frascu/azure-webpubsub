
package com.webpubsub.tutorial;

import com.azure.messaging.webpubsub.WebPubSubClientBuilder;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.GetAuthenticationTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubAuthenticationToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;

import com.google.gson.Gson;
import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        
        if (args.length != 1) {
            System.out.println("Expecting 1 arguments: <connection-string>");
            return;
        }

        // create the service client
        WebPubSubServiceClient client = new WebPubSubClientBuilder()
                .connectionString(args[0])
                .hub("chat")
                .buildClient();

        // start a server
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("public");
        }).start(8080);

        
        // Handle the negotiate request and return the token to the client
        app.get("/negotiate", ctx -> {
            String id = ctx.queryParam("id");
            if (id == null) {
                ctx.status(400);
                ctx.result("missing user id");
                return;
            }
            GetAuthenticationTokenOptions option = new GetAuthenticationTokenOptions();
            option.setUserId(id);
            WebPubSubAuthenticationToken token = client.getAuthenticationToken(option);
            ctx.result(token.getUrl());
        });
        
        // validation: https://azure.github.io/azure-webpubsub/references/protocol-cloudevents#validation
        app.options("/eventhandler", ctx -> {
            ctx.header("WebHook-Allowed-Origin", "*");
        });
    
        // handle events: https://azure.github.io/azure-webpubsub/references/protocol-cloudevents#events
        app.post("/eventhandler", ctx -> {
            String event = ctx.header("ce-type");
            if ("azure.webpubsub.sys.connected".equals(event)) {
                String id = ctx.header("ce-userId");
                client.sendToAll(String.format("[SYSTEM] %s joined", id), WebPubSubContentType.TEXT_PLAIN);
            } else if ("azure.webpubsub.sys.disconnected".equals(event)) {
                String id = ctx.header("ce-userId");
                client.sendToAll(String.format("[SYSTEM] %s disconnected", id), WebPubSubContentType.TEXT_PLAIN);
            } else if ("azure.webpubsub.user.message".equals(event)) {
                String id = ctx.header("ce-userId");
                MessageDTO messageDTO = new Gson().fromJson(ctx.body(), MessageDTO.class);

                if (messageDTO.getToUser() == null || messageDTO.getToUser().isEmpty()) {
                    client.sendToAll(String.format("[%s -> all] %s", id, messageDTO.getMessage()), WebPubSubContentType.TEXT_PLAIN);
                } else {
                    // send sender user
                    client.sendToUser(id, String.format("[%s -> %s] %s", id, messageDTO.getToUser(), messageDTO.getMessage()), WebPubSubContentType.TEXT_PLAIN);

                    // send recipient user
                    client.sendToUser(messageDTO.getToUser(), String.format("[%s -> you] %s", id, messageDTO.getMessage()), WebPubSubContentType.TEXT_PLAIN);
                }
            }
            ctx.status(200);
        });
    }
}