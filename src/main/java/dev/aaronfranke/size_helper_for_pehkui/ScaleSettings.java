package dev.aaronfranke.size_helper_for_pehkui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScaleSettings {
    private String playerName;
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

    public void applyScaleWithCommands() {
        final ArrayList<String> commands = new ArrayList<>();
        // 1.875 is the default Minecraft player height, turn it into a multiplier.
        final double bakedHeight = bakedHeightMeters / 1.875;
        final double height = heightMeters / 1.875;
        final double sqrtHeight = Math.sqrt(height);
        final double sqrtSqrtHeight = Math.sqrt(sqrtHeight);
        final double invSqrtHeight = 1.0 / sqrtHeight;
        final double invSqrtSqrtHeight = 1.0 / sqrtSqrtHeight;
        commands.add("scale set pehkui:height " + (height / bakedHeight) + " " + playerName);
        commands.add("scale set pehkui:width " + (height / bakedHeight) + " " + playerName);
        commands.add("scale set pehkui:knockback " + (height) + " " + playerName);
        commands.add("scale set pehkui:visibility " + (height) + " " + playerName);
        commands.add("scale set pehkui:attack " + (sqrtHeight * strength) + " " + playerName);
        commands.add("scale set pehkui:motion " + (sqrtHeight * motion / Math.sqrt(fatness)) + " " + playerName);
        commands.add("scale set pehkui:mining_speed " + (sqrtSqrtHeight) + " " + playerName);
        commands.add("scale set pehkui:attack_speed " + (invSqrtHeight) + " " + playerName);
        // Bigger characters have lower defense to allow their health to be drained faster,
        // which in turn makes them eat more food. The inverse is true for smaller characters.
        commands.add("scale set pehkui:defense " + (invSqrtHeight * strength / fatness) + " " + playerName);
        commands.add("scale set pehkui:health " + stepifyHealth(height * sqrtHeight * fatness) + " " + playerName);
        // For jump and step height, for small players we need to compensate for their small size,
        // for big players we need to compensate for their reduced motion relative to their size.
        if (height < 1.0) {
            commands.add("scale set pehkui:jump_height " + (invSqrtSqrtHeight * jumping) + " " + playerName);
            commands.add("scale set pehkui:step_height " + (invSqrtSqrtHeight) + " " + playerName);
        } else {
            commands.add("scale set pehkui:jump_height " + (sqrtSqrtHeight * jumping) + " " + playerName);
            commands.add("scale set pehkui:step_height " + (sqrtSqrtHeight) + " " + playerName);
        }
        // These values need special handling at small sizes.
        if (height < 0.25) {
            commands.add("scale set pehkui:view_bobbing 0.0 " + playerName);
            commands.add("scale set pehkui:reach 0.5 " + playerName);
        } else {
            commands.add("scale set pehkui:view_bobbing " + (sqrtHeight) + " " + playerName);
            if (height > 4.0) {
                commands.add("scale set pehkui:reach " + (height * 0.5) + " " + playerName);
            } else {
                commands.add("scale set pehkui:reach " + (sqrtHeight) + " " + playerName);
            }
        }
        if (height < 1.0) {
            commands.add("scale set pehkui:falling " + (height * height * Math.sqrt(fatness) / jumping) + " " + playerName);
        } else {
            commands.add("scale set pehkui:falling " + (Math.sqrt(fatness) / jumping) + " " + playerName);
        }
        // Special handling, not auto-calculated.
        commands.add("scale set pehkui:eye_height " + (eyeHeight * bakedHeight) + " " + playerName);
        commands.add("scale set pehkui:hitbox_height " + (hitboxHeight * bakedHeight) + " " + playerName);
        commands.add("scale set pehkui:hitbox_width " + (hitboxWidth * bakedHeight * Math.sqrt(fatness)) + " " + playerName);
        commands.add("scale set pehkui:third_person " + (thirdPersonDistance * bakedHeight) + " " + playerName);
        // Not pehkui.
        if (height > 1.0) {
            final double knockbackRes = 1.0 - 1.0 / (height * strength);
            commands.add("attribute " + playerName + " minecraft:generic.knockback_resistance base set " + knockbackRes);
        }
        if (flight.equals("creative")) {
            // Requires the "fly" command from the mod "fly-command-mod".
            commands.add("fly " + playerName + " allow");
        } else if (flight.equals("elytra")) {
            // Requires the "glide" command from the mod "MotHutils".
            commands.add("glide add " + playerName);
        } else if (!flight.equals("none")) {
            SizeHelperForPehkui.LOGGER.warn("Unknown flight type: " + flight + ". Allowed values are 'none', 'creative', and 'elytra'.");
        }
    }

    // Serialization functions.
    public static ScaleSettings fromHashMap(HashMap<String, Object> map) {
        ScaleSettings settings = new ScaleSettings();
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
}
