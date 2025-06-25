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
        StorageManager.StorageSaver();
    }

    // Adds a NamedLocation to the home list, returns true if it already exists
    public boolean addHome(NamedLocation home) throws Exception {
        if (getHome(home.getName()).isPresent()) {
            // Home with same name found!
            return true;

        } else {
            Homes.put(home.getName(), home);
            StorageManager.StorageSaver();
            return false;
        }
    }

    // -----

    public void deleteHome(NamedLocation home) throws Exception {
        Homes.remove(home.getName());

        StorageManager.StorageSaver();
    }
}
