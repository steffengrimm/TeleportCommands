package dev.mrsnowy.teleport_commands;

import dev.mrsnowy.teleport_commands.storage.StorageManager;
import dev.mrsnowy.teleport_commands.commands.*;

import dev.mrsnowy.teleport_commands.storage.DeathLocationStorage;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.nio.file.Paths;

import net.minecraft.core.BlockPos;

public class TeleportCommands {
	public static String MOD_LOADER;
	public static Path SAVE_DIR;
	public static Path CONFIG_DIR;
	public static MinecraftServer SERVER;

	// Gets ran when the server starts, initializes the mod :3
	public static void initializeMod(MinecraftServer server) {
		Constants.LOGGER.info("Initializing Teleport Commands (V{})! Hello {}!", Constants.VERSION, MOD_LOADER);

		SAVE_DIR = Path.of(String.valueOf(server.getWorldPath(LevelResource.ROOT)));
		CONFIG_DIR = Paths.get(System.getProperty("user.dir")).resolve("config"); // Construct the game directory path
		SERVER = server;

		StorageManager.StorageFromJSON();

		// initialize commands, also allows me to easily disable any when there is a config
		Commands commandManager = server.getCommands();
		back.register(commandManager);
		home.register(commandManager);
		tpa.register(commandManager);
		warp.register(commandManager);
		worldspawn.register(commandManager);
	}

	// Runs when the playerDeath mixin calls it, updates the /back command position
	public static void onPlayerDeath(ServerPlayer player) {
		BlockPos pos = new BlockPos(player.getBlockX(), player.getBlockY(), player.getBlockZ());
		String world = player.serverLevel().dimension().location().toString();
		String uuid = player.getStringUUID();

		DeathLocationStorage.setDeathLocation(uuid, pos, world);
	}
}