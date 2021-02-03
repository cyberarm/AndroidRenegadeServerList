package dev.cyberarm.cncnet_renegade_servers.library;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsSerializer implements JsonSerializer<Settings> {
    @Override
    public JsonElement serialize(Settings src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject container = new JsonObject();

        container.add("renegade_username", new JsonPrimitive(src.renegadeUsername));
        container.add("service_auto_refresh_interval", new JsonPrimitive(src.serviceAutoRefreshInterval));
        container.add("service_auto_start_at_boot", new JsonPrimitive(src.serviceAutoStartAtBoot));
        container.add("global_server_settings", context.serialize(src.globalServerSettings, ServerSettings.class));
        container.add("server_settings", context.serialize(src.serverSettings.toArray(), ServerSettings[].class));

        return container;
    }
}