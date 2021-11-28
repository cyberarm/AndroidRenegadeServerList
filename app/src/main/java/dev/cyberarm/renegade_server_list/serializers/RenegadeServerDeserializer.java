package dev.cyberarm.renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.cyberarm.renegade_server_list.library.RenegadePlayer;
import dev.cyberarm.renegade_server_list.library.RenegadeServer;

public class RenegadeServerDeserializer implements JsonDeserializer<RenegadeServer> {
    public RenegadeServer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String country = jsonObject.get("country").getAsString();
        final String countrycode = jsonObject.get("countrycode").getAsString();
        JsonElement _timeleft = jsonObject.get("timeleft");
        final String timeleft = (_timeleft instanceof JsonNull) ? "" : _timeleft.getAsString();
        final String ip = jsonObject.get("ip").getAsString();
        final int hostport = jsonObject.get("hostport").getAsInt();
        final String hostname = jsonObject.get("hostname").getAsString();
        final String mapname = jsonObject.get("mapname").getAsString();
        JsonElement _website = jsonObject.get("website");
        final String website = (_website instanceof JsonNull) ? "" : _website.getAsString();
        final int numplayers = jsonObject.get("numplayers").getAsInt();
        final int maxplayers = jsonObject.get("maxplayers").getAsInt();
        final String _password = jsonObject.get("password").getAsString();
        final boolean password = _password.equals("1");
        RenegadePlayer[] playersArray = context.deserialize(jsonObject.get("players"), RenegadePlayer[].class);

        List<RenegadePlayer> playersList = Arrays.asList(playersArray);
        ArrayList<RenegadePlayer> players = new ArrayList<>(playersList);

        return new RenegadeServer(country, countrycode, timeleft, ip, hostport, hostname, mapname, website, numplayers, maxplayers, password, players);
    }
}
