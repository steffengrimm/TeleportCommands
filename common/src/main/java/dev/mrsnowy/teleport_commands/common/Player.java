package dev.mrsnowy.teleport_commands.common;

import dev.mrsnowy.teleport_commands.storage.StorageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.unmodifiableList;

public class Player {
    private final String UUID;
    private String DefaultHome = "";
    private final HashMap<String, NamedLocation> Homes = new HashMap<>();

    public Player(String uuid) {
        this.UUID = uuid;
    }

    // -----

    public String getUUID() {
        return UUID;
    }

    public String getDefaultHome() {
        return DefaultHome;
    }

    // returns all homes
    public List<NamedLocation> getHomes() {
        return unmodifiableList(new ArrayList<>(Homes.values()));
    }

    // returns a specific home based on the name (if there is one)
    public Optional<NamedLocation> getHome(String name)  {
        return Optional.ofNullable(Homes.get(name));
    }

    // -----

    public void setDefaultHome(String defaultHome) throws Exception {
        this.DefaultHome = defaultHome;
        StorageManager.StorageToJSON();
    }

    // Adds a NamedLocation to the home list, returns true if it already exists
    public boolean addHome(NamedLocation home) throws Exception {
        if (Homes.containsKey(home.getName())) // Home with same name found!
            return true;
        Homes.put(home.getName(), home);
        StorageManager.StorageToJSON();
        return false;
    }

    public boolean renameHome(String oldName, String newName) throws Exception {
        if(!Homes.containsKey(oldName))
            return false;
        NamedLocation home = Homes.remove(oldName);
        home.setName(newName);
        Homes.put(newName, home);
        StorageManager.StorageToJSON();
        return true;
    }

    // -----

    public void deleteHome(NamedLocation home) throws Exception {
        Homes.remove(home.getName());

        StorageManager.StorageToJSON();
    }
}
