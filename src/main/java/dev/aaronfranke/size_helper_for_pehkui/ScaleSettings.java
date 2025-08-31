package dev.aaronfranke.size_helper_for_pehkui;

import net.minecraft.world.World;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.UUID;

public class ScaleSettings {
	public String playerName;
	private static final DecimalFormat STRINGIFY = new DecimalFormat("0.######");
	private static final double MOTION_ADJUST = 1.1;
	private static final boolean ALLOW_APRIL_FOOLS = true;
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

	// The amount that 'height' will be multiplied with upon a lightning strike.
	private double lightningGrowthMultiplier = 1.0;

	// Map that stores lightning UUID -> LightningGrowMoments, so they can be added together.
	private final HashMap<UUID, LightningGrowMoment> lightningGrowMoments = new HashMap<>();

	// Internal calculations.
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

	private HashMap<String, Double> calculatePehkuiScaleFactors() {
		final HashMap<String, Double> factors = new HashMap<>();
		// 1.875 is the default Minecraft player height, turn it into a multiplier.
		final double bakedHeightScale = bakedHeightMeters / 1.875;
		final double heightScale = Math.max(Math.min(getHeightMultiplier(), 40.0), 0.001);
		final double sqrtHeight = Math.sqrt(heightScale);
		final double sqrtSqrtHeight = Math.sqrt(sqrtHeight);
		final double sqrtSqrtSqrtHeight = Math.sqrt(sqrtSqrtHeight);
		final double invSqrtHeight = 1.0 / sqrtHeight;
		final double invSqrtSqrtHeight = 1.0 / sqrtSqrtHeight;
		final double squaredFatness = fatness * fatness;
		final double sqrtFatness = Math.sqrt(fatness);
		factors.put("height", heightScale / bakedHeightScale);
		factors.put("width", heightScale / bakedHeightScale);
		factors.put("knockback", heightScale);
		factors.put("visibility", heightScale);
		factors.put("attack", sqrtHeight * strength);
		factors.put("motion", MOTION_ADJUST * sqrtHeight * motion / sqrtFatness);
		factors.put("mining_speed", sqrtSqrtHeight);
		factors.put("attack_speed", invSqrtHeight);
		if (heightScale < 1.0) {
			// For jump and step height, for small players we need to compensate for their small size.
			factors.put("falling", heightScale * heightScale * sqrtFatness / jumping);
			factors.put("health", stepifyHealth(sqrtHeight * squaredFatness));
			factors.put("jump_height", invSqrtSqrtHeight * jumping);
			if (heightScale < 0.25) {
				factors.put("step_height", invSqrtSqrtHeight);
			} else {
				factors.put("step_height", invSqrtHeight);
			}
		} else {
			// For big players we need to compensate for their reduced motion relative to their size.
			factors.put("falling", sqrtFatness / jumping);
			factors.put("health", stepifyHealth(heightScale * sqrtHeight * squaredFatness));
			factors.put("jump_height", sqrtSqrtSqrtHeight * jumping);
			factors.put("step_height", sqrtSqrtHeight);
		}
		// Bigger characters have lower defense to allow their health to be drained faster,
		// which in turn makes them eat more food. The inverse is true for smaller characters.
		factors.put("defense", invSqrtHeight * strength / fatness);
		// These values need special handling at small sizes.
		if (heightScale > 3.16049) {
			factors.put("reach", heightScale * 0.75);
		} else if (heightScale > 1.0) {
			factors.put("reach", sqrtHeight * sqrtSqrtHeight);
		} else {
			// Realistically the reach should reduce at smaller sizes.
			// However, people keep getting annoyed by poor gameplay.
			factors.put("reach", 1.0);
		}
		if (heightScale < 0.25) {
			factors.put("view_bobbing", 0.0);
		} else if (heightScale < 2.0) {
			factors.put("view_bobbing", sqrtHeight);
		} else {
			factors.put("view_bobbing", 1.41421356237);
		}
		// Special handling, not auto-calculated.
		factors.put("eye_height", eyeHeight * bakedHeightScale);
		factors.put("hitbox_height", hitboxHeight * bakedHeightScale);
		factors.put("hitbox_width", hitboxWidth * bakedHeightScale * sqrtFatness);
		factors.put("third_person", thirdPersonDistance * bakedHeightScale);
		return factors;
	}

	public ArrayList<String> calculateScaleCommands(boolean disableUnused) {
		final ArrayList<String> commands = new ArrayList<>();
		// Pehkui.
		final HashMap<String, Double> factors = calculatePehkuiScaleFactors();
		for (String factor : factors.keySet()) {
			commands.add("scale set pehkui:" + factor + " " + STRINGIFY.format(factors.get(factor)) + " " + playerName);
		}
		// Not pehkui.
		final double height = heightMeters / 1.875;
		if (height > 1.0) {
			final double knockbackRes = 1.0 - 1.0 / (height * strength * (fatness * fatness));
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

	private boolean isAprilFoolsUTC() {
		if (!ALLOW_APRIL_FOOLS) {
			return false;
		}
		java.time.LocalDate utcDate = java.time.LocalDate.now(java.time.ZoneOffset.UTC);
		return utcDate.getMonth() == java.time.Month.APRIL && utcDate.getDayOfMonth() == 1;
	}

	public void onLightningStrike(UUID lightningUuid, World world) {
		if (lightningGrowthMultiplier == 1) {
			return;
		}
		// 900 seconds = 15 minutes. 15 minutes of growth from a lightning strike.
		lightningGrowMoments.putIfAbsent(lightningUuid, new LightningGrowMoment(900, world));
	}

	public double getHeightMultiplier() {
		double multiplier = heightMeters / 1.875;
		double totalMomentFraction = 0.0;
		final ArrayList<UUID> momentsToRemove = new ArrayList<>();

		for (UUID uuid : lightningGrowMoments.keySet()) {
			final LightningGrowMoment moment = lightningGrowMoments.get(uuid);
			if (moment.isFinished()) {
				momentsToRemove.add(uuid);
				continue;
			}
			totalMomentFraction += moment.getAdjustedScaleMultiplier();
		}
		momentsToRemove.forEach(lightningGrowMoments::remove);

		if (totalMomentFraction > 0.0) {
			// Blend between 1.0 and lightningGrowthMultiplier.
			final double effectiveGrowth = 1.0 + ((lightningGrowthMultiplier - 1.0) * totalMomentFraction);
			multiplier *= effectiveGrowth;
		}
		if (isAprilFoolsUTC()) {
			multiplier = 1.0 / multiplier;
		}
		if (multiplier < 0.002 / 1.875) {
			multiplier = 0.002 / 1.875;
		} else if (multiplier > 70.0 / 1.875) {
			multiplier = 70.0 / 1.875;
		}
		return multiplier;
	}

	public String stringifyCalculatedScaleFactors() {
		final StringBuilder sb = new StringBuilder();
		// Include the high-level sizes the user specifies.
		sb.append("\n").append(playerName).append(": ");
		final HashMap<String, Object> serialized = toHashMap();
		for (String key : serialized.keySet()) {
			sb.append(key).append("=").append(serialized.get(key)).append(", ");
		}
		if (isAprilFoolsUTC()) {
			sb.append("\nNote: April Fools is active, so the scale is inverted.");
		}
		// Also include the calculated scale factors.
		sb.append("\nCalculated Pehkui values: ");
		final HashMap<String, Double> factors = calculatePehkuiScaleFactors();
		final TreeSet<String> sortedFactorNames = new TreeSet<>(factors.keySet());
		for (String factor : sortedFactorNames) {
			sb.append(factor).append("=").append(STRINGIFY.format(factors.get(factor))).append(", ");
		}
		return sb.toString();
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
					return "The provided value of " + value + " meters is too small for Pehkui. Clipping to 0.002 (2 millimeters).";
				} else if (value > 70.0) {
					heightMeters = 70.0;
					return "The provided value of " + value + " meters is too big and would result in severe lag and server crashes. Clipping to 70 meters.";
				}
				heightMeters = value;
				if (heightMeters < 0.075) {
					return "Warning: Below 0.075 meters tall (7.5cm), path blocks will cover the camera.";
				}
				break;
			case "height_multiplier":
				return setScaleSetting("height_meters", value * 1.875);
			case "height_feet":
				return setScaleSetting("height_meters", value * 0.3048);
			case "height_inches":
				return setScaleSetting("height_meters", value * 0.0254);
			case "height_centimeters":
				return setScaleSetting("height_meters", value * 0.01);
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
			case "lightning_growth_multiplier":
				if (value <= 0) {
					lightningGrowthMultiplier = 1;
					return "Warning: lightning_growth_multiplier cannot be set to a non-positive number. Clipping to 1 (disabled).";
				}
				lightningGrowthMultiplier = value;
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
		if (map.containsKey("lightningGrowthMultiplier")) {
			settings.lightningGrowthMultiplier = (double) map.get("lightningGrowthMultiplier");
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
		if (lightningGrowthMultiplier != 1.0) {
			map.put("lightning_growth_multiplier", lightningGrowthMultiplier);
		}
		return map;
	}

	public ScaleSettings(String playerName) {
		this.playerName = playerName;
	}
}
