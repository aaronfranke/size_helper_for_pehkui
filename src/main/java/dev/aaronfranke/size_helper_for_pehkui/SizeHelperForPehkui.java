package dev.aaronfranke.size_helper_for_pehkui;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.aaronfranke.size_helper_for_pehkui.suggestion_providers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SizeHelperForPehkui implements ModInitializer {
	public static final String MOD_ID = "size_helper_for_pehkui";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Path MOD_CONFIG_PATH;
	private ConfigFile configFile;
	private SizeHelperCommandinator commandinator;
	// 20 seconds per tick, so 40 ticks is 2 seconds.
	private int RUN_COMMAND_INTERVAL_TICKS = 40;
	private int runCommandIntervalCounter = 0;

	// This code runs as soon as Minecraft is in a mod-load-ready state.
	// However, some things (like resources) may still be uninitialized.
	// Proceed with mild caution.
	@Override
	public void onInitialize() {
		// Load the config file.
		MOD_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".cfg");
		configFile = ConfigFile.loadFromFile(MOD_CONFIG_PATH, false);
		RUN_COMMAND_INTERVAL_TICKS = configFile.getInt("", "run_interval_ticks", RUN_COMMAND_INTERVAL_TICKS);
		commandinator = SizeHelperCommandinator.loadFromConfigFile(configFile);
		// Register commands that can be called by players to alter the config file.
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("size_helper")
				.requires(source -> source.hasPermissionLevel(1))
				.then(CommandManager.literal("delete")
					.then(CommandManager.argument("player_name", StringArgumentType.string())
						.suggests(new PlayerSuggestionProvider())
						.executes(commandinator::deletePlayerSizeScaling)
					)
				)
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("size_helper")
				.requires(source -> source.hasPermissionLevel(1))
				.then(CommandManager.literal("enum")
					.then(CommandManager.argument("enum_setting", StringArgumentType.string())
						.suggests(new EnumSettingSuggestionProvider())
						.then(CommandManager.argument("player_name", StringArgumentType.string())
							.suggests(new PlayerSuggestionProvider())
							.then(CommandManager.argument("enum_value", StringArgumentType.string())
								.suggests(new EnumValueSuggestionProvider())
								.executes(commandinator::setPlayerEnumString)
							)
						)
					)
				)
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("size_helper")
				.requires(source -> source.hasPermissionLevel(1))
				.then(CommandManager.literal("size")
					.then(CommandManager.argument("size_setting", StringArgumentType.string())
						.suggests(new ScaleSettingSuggestionProvider())
						.then(CommandManager.argument("player_name", StringArgumentType.string())
							.suggests(new PlayerSuggestionProvider())
							.then(CommandManager.argument("size_value", DoubleArgumentType.doubleArg())
								.executes(commandinator::setPlayerSizeScaling)
							)
						)
					)
				)
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("size_helper")
				.requires(source -> source.hasPermissionLevel(1))
				.then(CommandManager.literal("run_interval_ticks")
					.then(CommandManager.argument("run_interval_ticks", IntegerArgumentType.integer())
						.executes(this::setRunIntervalTicks)
					)
				)
			);
		});
		// Register a callback that re-runs all scale commands every tick.
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			runCommandIntervalCounter++;
			if (runCommandIntervalCounter > RUN_COMMAND_INTERVAL_TICKS) {
				runCommandIntervalCounter = 0;
				commandinator.runSizeScalingCommands(server, false);
			}
		});
		LOGGER.info("Loaded Size Helper for Pehkui by aaronfranke!");
	}

	private int setRunIntervalTicks(CommandContext<ServerCommandSource> context) {
		RUN_COMMAND_INTERVAL_TICKS = IntegerArgumentType.getInteger(context, "run_interval_ticks");
		configFile.setValue("", "run_interval_ticks", RUN_COMMAND_INTERVAL_TICKS);
		configFile.saveToFile(MOD_CONFIG_PATH);
		context.getSource().sendFeedback(() -> Text.literal("The size scaling commands will run every " + RUN_COMMAND_INTERVAL_TICKS + " ticks (" + (RUN_COMMAND_INTERVAL_TICKS / 20.0) + " seconds)."), false);
		return 1;
	}
}
