package dev.aaronfranke.size_helper_for_pehkui;

import java.util.ArrayList;
import java.util.HashMap;

public class ScaleSettings {
	public String playerName;
	private static final double MOTION_ADJUST = 1.1;
	// Most of the time, only height needs to be set, and the rest are calculated.
	// The other values provide another multiplier on top of the calculated value.
	// All numbers that don't say "meters" are multipliers compared to the default.

	// High-level properties, most automatically calculate Pehkui scale properties.
	private double heightMeters = 1.875;
	private double bakedHeightMeters = 1.875;
	private double fatness = 1.0;
	private double jumping = 1.0;
	private double strength = 1.0;
	private String flight = "none";

	// Low-level multipliers for Pehkui scale properties.
	private double eyeHeight = 1.0;
	private double hitboxHeight = 1.0;
	private double hitboxWidth = 1.0;
	private double motion = 1.0;
	private double thirdPersonDistance = 0.75;

	private double stepifyHealth(double health) {
		if (health < 0.2) {
			return 0.2;
		} else if (health < 2.0) {
			return Math.round(health * 10.0) / 10.0;
		} else if (health < 20.0) {
			return Math.round(health);
		}
		return Math.round(health / 10.0) * 10.0;
	}

	public ArrayList<String> calculateScaleCommands(boolean disableUnused) {
		final ArrayList<String> commands = new ArrayList<>();
		// 1.875 is the default Minecraft player height, turn it into a multiplier.
		final double bakedHeight = bakedHeightMeters / 1.875;
		final double height = heightMeters / 1.875;
		final double sqrtHeight = Math.sqrt(height);
		final double sqrtSqrtHeight = Math.sqrt(sqrtHeight);
		final double sqrtSqrtSqrtHeight = Math.sqrt(sqrtSqrtHeight);
		final double invSqrtHeight = 1.0 / sqrtHeight;
		final double invSqrtSqrtHeight = 1.0 / sqrtSqrtHeight;
		final double invSqrtSqrtSqrtHeight = 1.0 / sqrtSqrtSqrtHeight;
		commands.add("scale set pehkui:height " + (height / bakedHeight) + " " + playerName);
		commands.add("scale set pehkui:width " + (height / bakedHeight) + " " + playerName);
		commands.add("scale set pehkui:knockback " + (height) + " " + playerName);
		commands.add("scale set pehkui:visibility " + (height) + " " + playerName);
		commands.add("scale set pehkui:attack " + (sqrtHeight * strength) + " " + playerName);
		commands.add("scale set pehkui:motion " + (MOTION_ADJUST * sqrtHeight * motion / Math.sqrt(fatness)) + " " + playerName);
		commands.add("scale set pehkui:mining_speed " + (sqrtSqrtHeight) + " " + playerName);
		commands.add("scale set pehkui:attack_speed " + (invSqrtHeight) + " " + playerName);
		// Bigger characters have lower defense to allow their health to be drained faster,
		// which in turn makes them eat more food. The inverse is true for smaller characters.
		commands.add("scale set pehkui:defense " + (invSqrtHeight * strength / fatness) + " " + playerName);
		if (height < 1.0) {
			commands.add("scale set pehkui:health " + stepifyHealth(sqrtHeight * fatness) + " " + playerName);
		} else {
			commands.add("scale set pehkui:health " + stepifyHealth(height * sqrtHeight * fatness) + " " + playerName);
		}
		// For jump and step height, for small players we need to compensate for their small size.
		if (height < 0.25) {
			commands.add("scale set pehkui:jump_height " + (invSqrtSqrtSqrtHeight * jumping) + " " + playerName);
			commands.add("scale set pehkui:step_height " + (invSqrtSqrtHeight) + " " + playerName);
		} else if (height < 1.0) {
			commands.add("scale set pehkui:jump_height " + (invSqrtSqrtHeight * jumping) + " " + playerName);
			commands.add("scale set pehkui:step_height " + (invSqrtHeight) + " " + playerName);
		} else {
			// For big players we need to compensate for their reduced motion relative to their size.
			commands.add("scale set pehkui:jump_height " + (sqrtSqrtSqrtHeight * jumping) + " " + playerName);
			commands.add("scale set pehkui:step_height " + (sqrtSqrtHeight) + " " + playerName);
		}
		// These values need special handling at small sizes.
		if (height < 1.0) {
			commands.add("scale set pehkui:falling " + (height * height * Math.sqrt(fatness) / jumping) + " " + playerName);
		} else {
			commands.add("scale set pehkui:falling " + (Math.sqrt(fatness) / jumping) + " " + playerName);
		}
		if (height < 0.39685) {
			commands.add("scale set pehkui:reach 0.5 " + playerName);
		} else if (height > 3.16049) {
			commands.add("scale set pehkui:reach " + (height * 0.75) + " " + playerName);
		} else {
			commands.add("scale set pehkui:reach " + (sqrtHeight * sqrtSqrtHeight) + " " + playerName);
		}
		if (height < 0.25) {
			commands.add("scale set pehkui:view_bobbing 0.0 " + playerName);
		} else if (height < 2.0) {
			commands.add("scale set pehkui:view_bobbing " + (sqrtHeight) + " " + playerName);
		} else {
			commands.add("scale set pehkui:view_bobbing 1.41421356237 " + playerName);
		}
		// Special handling, not auto-calculated.
		commands.add("scale set pehkui:eye_height " + (eyeHeight * bakedHeight) + " " + playerName);
		commands.add("scale set pehkui:hitbox_height " + (hitboxHeight * bakedHeight) + " " + playerName);
		commands.add("scale set pehkui:hitbox_width " + (hitboxWidth * bakedHeight * Math.sqrt(fatness)) + " " + playerName);
		commands.add("scale set pehkui:third_person " + (thirdPersonDistance * bakedHeight) + " " + playerName);
		// Not pehkui.
		if (height > 1.0) {
			final double knockbackRes = 1.0 - 1.0 / (height * strength * fatness);
			commands.add("attribute " + playerName + " minecraft:generic.knockback_resistance base set " + knockbackRes);
		} else if (disableUnused) {
			commands.add("attribute " + playerName + " minecraft:generic.knockback_resistance base set 0.0");
		}
		switch (flight) {
			case "none":
				if (disableUnused) {
					commands.add("fly " + playerName + " noAllow");
					commands.add("glide remove " + playerName);
				}
				break;
			case "creative":
				// Requires the "fly" command from the mod "fly-command-mod".
				commands.add("fly " + playerName + " allow");
				break;
			case "elytra":
				// Requires the "glide" command from the mod "MotHutils".
				commands.add("glide add " + playerName);
				break;
			default:
				SizeHelperForPehkui.LOGGER.warn("Unknown flight type: {}. Allowed values are 'none', 'creative', and 'elytra'.", flight);
		}
		return commands;
	}

	public void setEnumSetting(String setting, String value) {
		switch (setting) {
			case "flight":
				if (value.equals("none") || value.equals("creative") || value.equals("elytra")) {
					flight = value;
				} else {
					SizeHelperForPehkui.LOGGER.warn("Unknown flight type: {}. Allowed values are 'none', 'creative', and 'elytra'.", value);
				}
				break;
			default:
				SizeHelperForPehkui.LOGGER.warn("Unknown enum setting: {}", setting);
		}
	}

	public String setScaleSetting(String setting, double value) {
		// Keep these cases in sync with the array at ScaleSettingSuggestionProvider.scaleSettingExposedNames.
		switch (setting) {
			case "height_meters":
				if (value < 0.002) {
					heightMeters = 0.002;
					return "The provided value of " + value + " is too small for Pehkui. Clipping to 0.002 (2 millimeters).";
				}
				heightMeters = value;
				if (heightMeters < 0.075) {
					return "Warning: Below 0.075 meters tall (7.5cm), path blocks will cover the camera.";
				}
				break;
			case "height_multiplier":
				setScaleSetting("height_meters", value * 1.875);
				break;
			case "height_feet":
				setScaleSetting("height_meters", value * 0.3048);
				break;
			case "height_inches":
				setScaleSetting("height_meters", value * 0.0254);
				break;
			case "height_centimeters":
				setScaleSetting("height_meters", value * 0.01);
				break;
			case "baked_height_meters":
				bakedHeightMeters = value;
				break;
			case "fatness":
				fatness = value;
				break;
			case "jumping":
				jumping = value;
				break;
			case "strength":
				strength = value;
				break;
			case "eye_height":
				eyeHeight = value;
				break;
			case "hitbox_height":
				hitboxHeight = value;
				break;
			case "hitbox_width":
				hitboxWidth = value;
				break;
			case "motion":
				motion = value;
				break;
			case "third_person_distance":
				thirdPersonDistance = value;
				break;
			default:
				SizeHelperForPehkui.LOGGER.warn("Unknown scale setting: " + setting);
		}
		return "";
	}

	// Serialization functions.
	public static ScaleSettings fromHashMap(HashMap<String, Object> map, String playerName) {
		ScaleSettings settings = new ScaleSettings(playerName);
		// High-level properties, most automatically calculate Pehkui scale properties.
		if (map.containsKey("height_meters")) {
			settings.heightMeters = (double) map.get("height_meters");
		}
		if (map.containsKey("baked_height_meters")) {
			settings.bakedHeightMeters = (double) map.get("baked_height_meters");
		}
		if (map.containsKey("fatness")) {
			settings.fatness = (double) map.get("fatness");
		}
		if (map.containsKey("jumping")) {
			settings.jumping = (double) map.get("jumping");
		}
		if (map.containsKey("strength")) {
			settings.strength = (double) map.get("strength");
		}
		if (map.containsKey("flight")) {
			settings.flight = (String) map.get("flight");
		}
		// Low-level multipliers for Pehkui scale properties.
		if (map.containsKey("eye_height")) {
			settings.eyeHeight = (double) map.get("eye_height");
		}
		if (map.containsKey("hitbox_height")) {
			settings.hitboxHeight = (double) map.get("hitbox_height");
		}
		if (map.containsKey("hitbox_width")) {
			settings.hitboxWidth = (double) map.get("hitbox_width");
		}
		if (map.containsKey("motion")) {
			settings.motion = (double) map.get("motion");
		}
		if (map.containsKey("third_person_distance")) {
			settings.thirdPersonDistance = (double) map.get("third_person_distance");
		}
		return settings;
	}

	public HashMap<String, Object> toHashMap() {
		HashMap<String, Object> map = new HashMap<>();
		// High-level properties, most automatically calculate Pehkui scale properties.
		// Always write height_meters, as it's the most important property.
		map.put("height_meters", heightMeters);
		if (bakedHeightMeters != 1.875) {
			map.put("baked_height_meters", bakedHeightMeters);
		}
		if (fatness != 1.0) {
			map.put("fatness", fatness);
		}
		if (jumping != 1.0) {
			map.put("jumping", jumping);
		}
		if (strength != 1.0) {
			map.put("strength", strength);
		}
		if (!flight.equals("none")) {
			map.put("flight", flight);
		}
		// Low-level multipliers for Pehkui scale properties.
		if (eyeHeight != 1.0) {
			map.put("eye_height", eyeHeight);
		}
		if (hitboxHeight != 1.0) {
			map.put("hitbox_height", hitboxHeight);
		}
		if (hitboxWidth != 1.0) {
			map.put("hitbox_width", hitboxWidth);
		}
		if (motion != 1.0) {
			map.put("motion", motion);
		}
		if (thirdPersonDistance != 0.75) {
			map.put("third_person_distance", thirdPersonDistance);
		}
		return map;
	}

	public ScaleSettings(String playerName) {
		this.playerName = playerName;
	}
}
