package dev.aaronfranke.size_helper_for_pehkui;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class SizeHelperCommandinator {
	private ConfigFile configFile = new ConfigFile();
	private final HashMap<String, ScaleSettings> allScaleSettings = new HashMap<>();

	// Runs every frame as registered by SizeHelperForPehkui.
	public void runSizeScalingCommands(MinecraftServer server, boolean disableUnused) {
		server.execute(() -> {
			ServerCommandSource source = server.getCommandSource();
			CommandManager commandManager = server.getCommandManager();
			Collection<String> playerNames = source.getPlayerNames();
			for (String playerName : playerNames) {
				if (allScaleSettings.containsKey(playerName)) {
					ScaleSettings settings = allScaleSettings.get(playerName);
					ArrayList<String> commands = settings.calculateScaleCommands(disableUnused);
					for (String command : commands) {
						commandManager.executeWithPrefix(source, command);
					}
				}
			}
		});
	}

	// Runs whenever the "/size_helper delete player_name" command is run.
	public int deletePlayerSizeScaling(CommandContext<ServerCommandSource> context) {
		final String playerName = StringArgumentType.getString(context, "player_name");
		if (allScaleSettings.containsKey(playerName)) {
			allScaleSettings.remove(playerName);
			configFile.deleteSection(playerName);
			context.getSource().sendFeedback(() -> Text.literal("Deleted size scaling config for " + playerName + "."), false);
		} else {
			context.getSource().sendFeedback(() -> Text.literal("No size scaling config found for " + playerName + "."), false);
		}
		saveToConfigFile(false);
		runSizeScalingCommands(context.getSource().getServer(), true);
		return 1;
	}

	// Runs whenever the "/size_helper enum player_name enum_setting enum_value" command is run.
	public int setPlayerEnumString(CommandContext<ServerCommandSource> context) {
		final String playerName = StringArgumentType.getString(context, "player_name");
		final String enumSetting = StringArgumentType.getString(context, "enum_setting");
		final String enumValue = StringArgumentType.getString(context, "enum_value");
		ScaleSettings scaleSettings;
		if (allScaleSettings.containsKey(playerName)) {
			scaleSettings = allScaleSettings.get(playerName);
		} else {
			scaleSettings = new ScaleSettings(playerName);
			allScaleSettings.put(playerName, scaleSettings);
		}
		scaleSettings.setEnumSetting(enumSetting, enumValue);
		configFile.setSectionData(playerName, scaleSettings.toHashMap());
		saveToConfigFile(false);
		runSizeScalingCommands(context.getSource().getServer(), true);
		return 1;
	}

	// Runs whenever the "/size_helper size player_name size_setting size_value" command is run.
	public int setPlayerSizeScaling(CommandContext<ServerCommandSource> context) {
		final String playerName = StringArgumentType.getString(context, "player_name");
		final String sizeSetting = StringArgumentType.getString(context, "size_setting");
		final double sizeValue = DoubleArgumentType.getDouble(context, "size_value");
		ScaleSettings scaleSettings;
		if (allScaleSettings.containsKey(playerName)) {
			scaleSettings = allScaleSettings.get(playerName);
		} else {
			scaleSettings = new ScaleSettings(playerName);
			allScaleSettings.put(playerName, scaleSettings);
		}
		final String ret = scaleSettings.setScaleSetting(sizeSetting, sizeValue);
		if (!ret.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.literal(ret), false);
		}
		configFile.setSectionData(playerName, scaleSettings.toHashMap());
		saveToConfigFile(false);
		runSizeScalingCommands(context.getSource().getServer(), true);
		return 1;
	}

	public int viewCalculatedPlayerScaling(CommandContext<ServerCommandSource> context) {
		final String playerName = StringArgumentType.getString(context, "player_name");
		if (allScaleSettings.containsKey(playerName)) {
			ScaleSettings settings = allScaleSettings.get(playerName);
			context.getSource().sendFeedback(() -> Text.literal(settings.stringifyCalculatedScaleFactors()), false);
		} else {
			context.getSource().sendFeedback(() -> Text.literal("Size helper is not scaling " + playerName + "."), false);
		}
		return 1;
	}

	// Loading and saving functions.
	public static SizeHelperCommandinator loadFromConfigFile(ConfigFile configFile) {
		SizeHelperCommandinator commandinator = new SizeHelperCommandinator();
		commandinator.configFile = configFile;
		Set<String> sectionNames = configFile.getSectionNames();
		for (String sectionName : sectionNames) {
			if (!sectionName.isEmpty()) {
				ScaleSettings settings = ScaleSettings.fromHashMap(configFile.getSectionData(sectionName), sectionName);
				settings.playerName = sectionName;
				commandinator.allScaleSettings.put(sectionName, settings);
			}
		}
		return commandinator;
	}

	public void saveToConfigFile(boolean reserialize) {
		if (reserialize) {
			configFile = new ConfigFile();
			for (String playerName : allScaleSettings.keySet()) {
				ScaleSettings settings = allScaleSettings.get(playerName);
				configFile.setSectionData(playerName, settings.toHashMap());
			}
		}
		configFile.saveToFile(SizeHelperForPehkui.MOD_CONFIG_PATH);
	}
}
