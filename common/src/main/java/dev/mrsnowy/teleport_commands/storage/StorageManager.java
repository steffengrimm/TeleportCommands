package dev.mrsnowy.teleport_commands.storage;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.TeleportCommands;
import dev.mrsnowy.teleport_commands.common.NamedLocation;
import dev.mrsnowy.teleport_commands.common.Player;
import dev.mrsnowy.teleport_commands.utils.HashMapSerde;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public class StorageManager {
    public static Path STORAGE_FOLDER;
    public static Path STORAGE_FILE;
    public static StorageClass STORAGE;
    private static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Type namedLocationMap = new TypeToken<HashMap<String, NamedLocation>>() {
        }.getType();
        gsonBuilder.registerTypeAdapter(namedLocationMap, new HashMapSerde<>("name", NamedLocation.class));

        Type playerMap = new TypeToken<HashMap<String, Player>>() {
        }.getType();
        gsonBuilder.registerTypeAdapter(playerMap, new HashMapSerde<>("UUID", Player.class));
        gson = gsonBuilder.create();
    }

    private static void StorageInit() {
        STORAGE_FOLDER = TeleportCommands.SAVE_DIR.resolve("TeleportCommands/");
        STORAGE_FILE = STORAGE_FOLDER.resolve("storage.json");

        try {
            // check if the folder exists and create it
            if (!Files.exists(STORAGE_FOLDER)) {
                Files.createDirectories(STORAGE_FOLDER);
            }

            // check if the file exists and create it
            if (!Files.exists(STORAGE_FILE)) {
                Files.createFile(STORAGE_FILE);
            }

            // create the basic storage if it is empty
            if (new File(String.valueOf(STORAGE_FILE)).length() == 0) {
                STORAGE = new StorageClass();
                StorageToJSON(); // todo! verify that it creates em correctly
            }

        } catch (Exception e) {
            Constants.LOGGER.error("Error while creating the storage file! Exiting! => ", e);
            // crashing is probably better here, otherwise the whole mod will be broken
            System.exit(1);
        }
    }

    public static void StorageToJSON() throws Exception {
        // todo! maybe throttle saves?
        byte[] json = gson.toJson(STORAGE).getBytes();

        Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void StorageFromJSON() {
        Constants.LOGGER.info("Cleaning and updating Storage!");

        // cleans and updates Storage to the newest "version". This is painful
        try {
            StorageInit();

            long startFileSize = Files.size(STORAGE_FILE);

            FileReader reader = new FileReader(STORAGE_FILE.toString());
            JsonElement jsonElement = JsonParser.parseReader(reader);

            if (jsonElement.isJsonObject()) {

                JsonObject mainJsonObject = jsonElement.getAsJsonObject();
                JsonArray newWarpsArray = new JsonArray();
                JsonArray newPlayersArray = new JsonArray();

                // get the Warps list
                if (mainJsonObject.has("Warps") && mainJsonObject.get("Warps").isJsonArray()) {

                    // Warps
                    for (JsonElement warpElement : mainJsonObject.get("Warps").getAsJsonArray()) {

                        // Warp
                        if (warpElement.isJsonObject()) {
                            JsonObject warp = warpElement.getAsJsonObject();

                            String warpName = warp.has("name") ? warp.get("name").getAsString() : "";
                            Integer warpX = warp.has("x") ? warp.get("x").getAsInt() : null;
                            Integer warpY = warp.has("y") ? warp.get("y").getAsInt() : null;
                            Integer warpZ = warp.has("z") ? warp.get("z").getAsInt() : null;
                            String warpWorld = warp.has("world") ? warp.get("world").getAsString() : "";

                            // check if it is valid
                            if (!warpName.isBlank() && !warpWorld.isBlank() && warpX != null && warpY != null
                                    && warpZ != null) {
                                JsonObject newWarp = new JsonObject();

                                newWarp.addProperty("name", warpName);
                                newWarp.addProperty("x", warpX);
                                newWarp.addProperty("y", warpY);
                                newWarp.addProperty("z", warpZ);
                                newWarp.addProperty("world", warpWorld);

                                newWarpsArray.add(newWarp);
                            }
                        }
                    }
                }

                // get the Players list
                if (mainJsonObject.has("Players") && mainJsonObject.get("Players").isJsonArray()) {

                    // players
                    for (JsonElement playerElement : mainJsonObject.get("Players").getAsJsonArray()) {

                        // player
                        if (playerElement.isJsonObject()) {

                            JsonObject player = playerElement.getAsJsonObject();
                            boolean hasInformation = false;

                            String UUID = player.has("Player_UUID")
                                    ? player.get("Player_UUID").getAsString()
                                    : (player.has("UUID")
                                            ? player.get("UUID").getAsString()
                                            : null);

                            String DefaultHome = player.has("DefaultHome")
                                    ? player.get("DefaultHome").getAsString()
                                    : "";

                            JsonArray homes = new JsonArray();

                            if (player.has("Homes") && player.get("Homes").isJsonArray()) {
                                JsonArray tempHomes = player.get("Homes").getAsJsonArray();
                                boolean defaultHomeFound = false;

                                for (JsonElement homeElement : tempHomes) {
                                    if (homeElement.isJsonObject()) {
                                        JsonObject home = homeElement.getAsJsonObject();

                                        String homeName = home.has("name")
                                                ? home.get("name").getAsString()
                                                : "";

                                        // upgrade doubles to int
                                        Integer homeX = home.has("x") && home.get("x").isJsonPrimitive()
                                                && home.get("x").getAsJsonPrimitive().isNumber()
                                                        ? (int) Math.floor(home.get("x").getAsDouble())
                                                        : null;

                                        Integer homeY = home.has("y") && home.get("y").isJsonPrimitive()
                                                && home.get("y").getAsJsonPrimitive().isNumber()
                                                        ? (int) Math.floor(home.get("y").getAsDouble())
                                                        : null;

                                        Integer homeZ = home.has("z") && home.get("z").isJsonPrimitive()
                                                && home.get("z").getAsJsonPrimitive().isNumber()
                                                        ? (int) Math.floor(home.get("z").getAsDouble())
                                                        : null;

                                        String homeWorld = home.has("world")
                                                ? home.get("world").getAsString()
                                                : "";

                                        // check if it is valid
                                        if (!homeName.isBlank() && !homeWorld.isBlank() && homeX != null
                                                && homeY != null && homeZ != null) {

                                            // check if it is the default home
                                            if (!DefaultHome.isBlank() && homeName.equals(DefaultHome)) {
                                                defaultHomeFound = true;
                                            }

                                            JsonObject newHome = new JsonObject();

                                            newHome.addProperty("name", homeName);
                                            newHome.addProperty("x", homeX);
                                            newHome.addProperty("y", homeY);
                                            newHome.addProperty("z", homeZ);
                                            newHome.addProperty("world", homeWorld);

                                            homes.add(newHome);
                                            hasInformation = true;
                                        }
                                    }
                                }

                                // clean DefaultHome if there is no home with the name
                                if (!defaultHomeFound) {
                                    DefaultHome = "";
                                }
                            }

                            // if it isn't empty it gets added to the newPlayersArray
                            if ((UUID != null && !UUID.isBlank()) && hasInformation) {

                                JsonObject newPlayer = new JsonObject();

                                newPlayer.addProperty("UUID", UUID);
                                newPlayer.addProperty("DefaultHome", DefaultHome);
                                newPlayer.add("Homes", homes);

                                newPlayersArray.add(newPlayer);
                            }
                        }
                    }
                }

                // save the cleaned and updated file
                mainJsonObject.remove("Warps");
                mainJsonObject.add("Warps", newWarpsArray);

                mainJsonObject.remove("Players");
                mainJsonObject.add("Players", newPlayersArray);

                byte[] json = gson.toJson(mainJsonObject).getBytes();
                Files.write(STORAGE_FILE, json, StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);

                // Only show amount cleaned when it isn't 0B lool
                int diff = Math.round((startFileSize - Files.size(STORAGE_FILE)));

                if (diff > 0) {
                    Constants.LOGGER.info("Success! Cleaned: {}B", diff);
                } else {
                    Constants.LOGGER.info("Success!");
                }

                STORAGE = gson.fromJson(mainJsonObject, StorageClass.class);
            }

        } catch (IOException e) {
            Constants.LOGGER.error("Error while cleaning the database!", e);
        }
    }

    public static class StorageClass {
        private final HashMap<String, NamedLocation> Warps = new HashMap<>();
        private final HashMap<String, Player> Players = new HashMap<>();

        // -----

        // returns all warps
        public List<NamedLocation> getWarps() {
            return unmodifiableList(new ArrayList<>(Warps.values()));
        }

        // filters the warpList and finds the one with the name (if there is one)
        public Optional<NamedLocation> getWarp(String name) {
            return Optional.ofNullable(Warps.get(name));
        }

        // filters the playerList and finds the one with the uuid (if there is one)
        public Optional<Player> getPlayer(String uuid) {
            return Optional.ofNullable(Players.get(uuid));
        }

        // -----

        // Adds a NamedLocation to the warp list, returns true if a warp with the same
        // name already exists
        public boolean addWarp(NamedLocation warp) throws Exception {
            if (getWarp(warp.getName()).isPresent()) {
                // Warp with same name found!
                return true;

            } else {
                Warps.put(warp.getName(), warp);
                StorageToJSON();
                return false;
            }
        }

        // Creates a new player, if there already is a player it will return the
        // existing one. The player won't be saved unless they actually do something lol
        // The name of this function is wack but whatever kewk
        public Player addPlayer(String uuid) {
            final Optional<Player> OptionalPlayer = getPlayer(uuid);

            if (OptionalPlayer.isEmpty()) {
                // create and return new player
                Player player = new Player(uuid);
                Players.put(uuid, player);

                return player;
            } else {
                // return existing player
                return OptionalPlayer.get();
            }
        }

        // -----

        // Remove a warp, if the warp isn't found then nothing will happen
        public void removeWarp(NamedLocation warp) throws Exception {
            Warps.remove(warp.getName());
            StorageToJSON();
        }
    }
}
