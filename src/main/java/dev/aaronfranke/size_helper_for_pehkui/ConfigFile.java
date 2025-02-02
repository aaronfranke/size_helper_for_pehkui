package dev.aaronfranke.size_helper_for_pehkui;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

// INI-style config file, similar to Godot's ConfigFile class.
public class ConfigFile {
    private HashMap<String, HashMap<String, Object>> _data;

    // Generic accessor functions.
    public HashMap<String, Object> getSectionData(String section) {
        HashMap<String, Object> sectionData = _data.get(section);
        if (sectionData == null) {
            sectionData = new HashMap<>();
            _data.put(section, sectionData);
        }
        return sectionData;
    }

    public void setSectionData(String section, HashMap<String, Object> data) {
        _data.put(section, data);
    }

    public Object getValue(String section, String key, Object defaultValue) {
        HashMap<String, Object> sectionData = getSectionData(section);
        if (sectionData.containsKey(key)) {
            return sectionData.get(key);
        }
        sectionData.put(key, defaultValue);
        return defaultValue;
    }

    public void setValue(String section, String key, Object value) {
        HashMap<String, Object> sectionData = getSectionData(section);
        sectionData.put(key, value);
    }

    // Typed getter functions.
    public String getString(String section, String key, String defaultValue) {
        return getValue(section, key, defaultValue).toString();
    }

    public double getDouble(String section, String key, double defaultValue) {
        Object value = getValue(section, key, defaultValue);
        if (value instanceof Double) {
            return ((Double)value).doubleValue();
        } else if (value instanceof Integer) {
            return ((Integer)value).doubleValue();
        }
        return defaultValue;
    }

    public int getInt(String section, String key, int defaultValue) {
        Object value = getValue(section, key, defaultValue);
        if (value instanceof Integer) {
            return ((Integer) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        }
        return defaultValue;
    }

    // File I/O functions.
    private static ConfigFile loadFromLines(List<String> lines) {
        final ConfigFile configFile = new ConfigFile();
        // Start with the global section.
        HashMap<String, Object> currentSectionData = configFile.getSectionData("");
        for (String line : lines) {
            if (line.startsWith(";") || line.startsWith("#")) {
                // Comment.
                continue;
            }
            if (line.startsWith("[")) {
                final String sectionName = line.substring(1, line.length() - 1);
                currentSectionData = configFile.getSectionData(sectionName);
            } else {
                final String[] parts = line.split("=");
                if (parts.length == 2) {
                    final String key = parts[0];
                    final String value = parts[1];
                    final char firstChar = value.charAt(0);
                    if (value.charAt(0) == '"') {
                        // String value (need to strip quotes).
                        currentSectionData.put(key, value.substring(1, value.length() - 1));
                    } else if (value.equals("true")) {
                        currentSectionData.put(key, Boolean.TRUE);
                    } else if (value.equals("false")) {
                        currentSectionData.put(key, Boolean.FALSE);
                    } else if (value.contains(".")) {
                        try {
                            currentSectionData.put(key, Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            SizeHelperForPehkui.LOGGER.warn("ConfigFile: Value contained a dot, but couldn't be parsed as a double: " + value);
                        }
                    } else if (firstChar >= '0' && firstChar <= '9' || (firstChar == '-' && value.length() > 1 && value.charAt(1) >= '0' && value.charAt(1) <= '9')) {
                        try {
                            currentSectionData.put(key, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            SizeHelperForPehkui.LOGGER.warn("ConfigFile: Value is numeric, but couldn't be parsed as an integer: " + value);
                        }
                    } else {
                        SizeHelperForPehkui.LOGGER.warn("ConfigFile: Value couldn't be parsed: " + value);
                    }
                }
            }
        }
        return configFile;
    }

    public static ConfigFile loadFromFile(Path path, boolean warnIfMissing) {
        try {
            Files.readAllLines(path);
            return loadFromLines(Files.readAllLines(path));
        } catch (java.io.IOException e) {
            if (warnIfMissing) {
                SizeHelperForPehkui.LOGGER.warn("ConfigFile: Failed to read file: " + path + ". Returning an empty ConfigFile.");
            }
            return new ConfigFile();
        }
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();
        TreeSet<String> sections = new TreeSet<>(_data.keySet());
        for (String section : sections) {
            if (!section.isEmpty()) {
                builder.append("\n[").append(section).append("]\n");
            }
            HashMap<String, Object> sectionData = _data.get(section);
            Set<String> keys = sectionData.keySet();
            for (String key : keys) {
                Object value = sectionData.get(key);
                if (value instanceof String) {
                    builder.append(key).append("=\"").append(value).append("\"\n");
                } else {
                    builder.append(key).append("=").append(value).append("\n");
                }
            }
        }
        return builder.toString();
    }

    public void saveToFile(Path path) {
        try {
            Files.write(path, toString().getBytes("UTF-8"));
        } catch (java.io.IOException e) {
            SizeHelperForPehkui.LOGGER.warn("ConfigFile: Failed to write file: " + path);
        }
    }

    public ConfigFile() {
        _data = new HashMap<>();
    }
}
