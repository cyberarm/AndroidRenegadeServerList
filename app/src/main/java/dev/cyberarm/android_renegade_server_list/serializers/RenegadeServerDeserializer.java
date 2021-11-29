package dev.cyberarm.android_renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.cyberarm.android_renegade_server_list.library.RenegadeServer;
import dev.cyberarm.android_renegade_server_list.library.RenegadeServerStatus;

public class RenegadeServerDeserializer implements JsonDeserializer<RenegadeServer> {
    public RenegadeServer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String id = jsonObject.get("id").getAsString();
        final String game = jsonObject.get("game").getAsString();
        final String address = jsonObject.get("address").getAsString();
        final int port = jsonObject.get("port").getAsInt();
        final String region = jsonObject.get("region").getAsString();

        RenegadeServerStatus status = context.deserialize(jsonObject.get("status"), RenegadeServerStatus.class);

        return new RenegadeServer(id, game, address, port, region, status);
    }
}
