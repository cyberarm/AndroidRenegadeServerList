package dev.cyberarm.renegade_server_list.serializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import dev.cyberarm.renegade_server_list.library.RenegadeServer;
import dev.cyberarm.renegade_server_list.library.RenegadeServerStatus;
import dev.cyberarm.renegade_server_list.library.RenegadeServerStatusPlayer;
import dev.cyberarm.renegade_server_list.library.RenegadeServerStatusTeam;
import java8.util.stream.StreamSupport;

public class RenegadeServerStatusDeserializer implements JsonDeserializer<RenegadeServerStatus> {
    public RenegadeServerStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        final String name = jsonObject.get("name").getAsString();
        final String map = jsonObject.get("map").getAsString();
        final String remaining = jsonObject.get("remaining").getAsString();
        final int maxPlayers = jsonObject.get("maxplayers").getAsInt();
        String started = jsonObject.get("started").getAsString();
        JsonElement _password = jsonObject.get("password");
        System.out.println(_password);
        final boolean password = _password == null ? false : _password.getAsBoolean();

        RenegadeServerStatusTeam[] teamsArray = context.deserialize(jsonObject.get("teams"), RenegadeServerStatusTeam[].class);

        List<RenegadeServerStatusTeam> teamsList = Arrays.asList(teamsArray);
        ArrayList<RenegadeServerStatusTeam> teams = new ArrayList<>(teamsList);

        RenegadeServerStatusPlayer[] playersArray = context.deserialize(jsonObject.get("players"), RenegadeServerStatusPlayer[].class);

        List<RenegadeServerStatusPlayer> playersList = Arrays.asList(playersArray);
        ArrayList<RenegadeServerStatusPlayer> players = new ArrayList<>(playersList);

        java8.lang.Iterables.removeIf(players, player -> (player.nick.equals("GDI") || player.nick.equals("Nod") ));

        return new RenegadeServerStatus(name, map, remaining, players.size(), maxPlayers, started, password, teams, players);
    }
}
