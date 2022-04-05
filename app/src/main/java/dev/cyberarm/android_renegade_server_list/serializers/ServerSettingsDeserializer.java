package dev.cyberarm.android_renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import dev.cyberarm.android_renegade_server_list.library.ServerSettings;

public class ServerSettingsDeserializer implements JsonDeserializer<ServerSettings> {
    @Override
    public ServerSettings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String ID = jsonObject.get("id").getAsString();
        JsonElement aName = jsonObject.get("name");
        final String name = (aName == null) ? "" : aName.getAsString();
        final int notifyPlayerCount = jsonObject.get("notify_player_count").getAsInt();
        final String[] mapNames = context.deserialize(jsonObject.get("notify_map_names"), String[].class);
        final String[] usernames = context.deserialize(jsonObject.get("notify_usernames"), String[].class);

        ArrayList<String> notifyMapNames = new ArrayList<>(Arrays.asList(mapNames));
        ArrayList<String> notifyUsernames = new ArrayList<>(Arrays.asList(usernames));

        JsonElement aBoolean = jsonObject.get("notify_require_multiple_conditions");
        final boolean notifyRequireMultipleConditions = aBoolean != null && aBoolean.getAsBoolean();

        return new ServerSettings(ID, name, notifyPlayerCount, notifyMapNames, notifyUsernames, notifyRequireMultipleConditions);
    }
}
