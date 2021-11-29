package dev.cyberarm.android_renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.cyberarm.android_renegade_server_list.library.RenegadeServerStatusTeam;

public class RenegadeServerStatusTeamDeserializer implements JsonDeserializer<RenegadeServerStatusTeam> {
    @Override
    public RenegadeServerStatusTeam deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final int id = jsonObject.get("id").getAsInt();
        final String name = jsonObject.get("name").getAsString();
        final int score = jsonObject.get("score").getAsInt();
        final int kills = jsonObject.get("kills").getAsInt();
        final int deaths = jsonObject.get("deaths").getAsInt();

        return new RenegadeServerStatusTeam(id, name, score, kills, deaths);
    }
}
