package dev.aaronfranke.size_helper_for_pehkui;

import com.mojang.brigadier.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SizeHelperForPehkui implements ModInitializer {
	public static final String MOD_ID = "size_helper_for_pehkui";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static Path MOD_CONFIG_PATH;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		MOD_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".cfg");

		ConfigFile configFile = ConfigFile.loadFromFile(MOD_CONFIG_PATH, false);
		LOGGER.info(configFile.getValue("", "test", 1.0).toString());
		configFile.setValue("", "test", "hello");
		configFile.saveToFile(MOD_CONFIG_PATH);

		LOGGER.info("Hello Fabric world!");
	}
}
