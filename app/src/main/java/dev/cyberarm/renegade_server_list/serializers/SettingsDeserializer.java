package dev.cyberarm.renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import dev.cyberarm.renegade_server_list.library.ServerSettings;
import dev.cyberarm.renegade_server_list.library.Settings;

public class SettingsDeserializer implements JsonDeserializer<Settings> {
    @Override
    public Settings deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String renegadeUsername = jsonObject.get("renegade_username").getAsString();
        final int serviceAutoRefreshInterval = jsonObject.get("service_auto_refresh_interval").getAsInt();
        final boolean serviceAutoStartAtBoot = jsonObject.get("service_auto_start_at_boot").getAsBoolean();
        final ServerSettings globalServerSettings = context.deserialize(jsonObject.get("global_server_settings"), ServerSettings.class);
        final ServerSettings[] _serverSettings = context.deserialize(jsonObject.get("server_settings"), ServerSettings[].class);

        ArrayList<ServerSettings> serverSettings = new ArrayList<>(Arrays.asList(_serverSettings));

        return new Settings(renegadeUsername, serviceAutoRefreshInterval, serviceAutoStartAtBoot, globalServerSettings, serverSettings);
    }
}
