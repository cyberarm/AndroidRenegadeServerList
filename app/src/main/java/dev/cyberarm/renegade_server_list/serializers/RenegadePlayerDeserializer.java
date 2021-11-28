package dev.cyberarm.renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.cyberarm.renegade_server_list.library.RenegadePlayer;

public class RenegadePlayerDeserializer implements JsonDeserializer<RenegadePlayer> {
    @Override
    public RenegadePlayer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String name = jsonObject.get("name").getAsString();
        final String team = jsonObject.get("team").getAsString();
        final int score = jsonObject.get("score").getAsInt();
        final int kills = jsonObject.get("kills").getAsInt();
        final int deaths = jsonObject.get("deaths").getAsInt();
        final int ping = jsonObject.get("ping").getAsInt();

        return new RenegadePlayer(name, team, score, kills, deaths, ping);
    }
}
