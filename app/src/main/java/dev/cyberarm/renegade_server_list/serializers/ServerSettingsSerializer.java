package dev.cyberarm.renegade_server_list.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;

import dev.cyberarm.renegade_server_list.library.ServerSettings;

public class ServerSettingsSerializer implements JsonSerializer<ServerSettings> {
    @Override
    public JsonElement serialize(ServerSettings src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject container = new JsonObject();

        container.add("id", new JsonPrimitive(src.ID));
        container.add("notify_player_count", new JsonPrimitive(src.notifyPlayerCount));
        container.add("notify_map_names", context.serialize(src.notifyMapNames.toArray(), ArrayList[].class));
        container.add("notify_usernames", context.serialize(src.notifyUsernames.toArray(), ArrayList[].class));

        return container;
    }
}
