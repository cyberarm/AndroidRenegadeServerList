package dev.cyberarm.cncnet_renegade_servers.library;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerSettingsDeserializer implements JsonDeserializer<ServerSettings> {
    @Override
    public ServerSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String ID = jsonObject.get("id").getAsString();
        final int notifyPlayerCount = jsonObject.get("notify_player_count").getAsInt();
        final String[] mapNames = context.deserialize(jsonObject.get("notify_map_names"), String[].class);
        final String[] usernames = context.deserialize(jsonObject.get("notify_usernames"), String[].class);

        ArrayList<String> notifyMapNames = new ArrayList<>(Arrays.asList(mapNames));
        ArrayList<String> notifyUsernames = new ArrayList<>(Arrays.asList(usernames));

        return new ServerSettings(ID, notifyPlayerCount, notifyMapNames, notifyUsernames);
    }
}
