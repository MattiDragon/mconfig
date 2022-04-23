package io.github.mattidragon.mconfig.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    public static final List<Config<?>> CLIENT_CONFIGS = new ArrayList<>();
    public static final List<Config<?>> COMMON_CONFIGS = new ArrayList<>();
    public static final List<Config<?>> SERVER_CONFIGS = new ArrayList<>();
    public static final ImmutableMap<ConfigType, List<Config<?>>> LOOKUP = Maps.immutableEnumMap(Map.of(ConfigType.COMMON, COMMON_CONFIGS, ConfigType.SERVER, SERVER_CONFIGS, ConfigType.CLIENT, CLIENT_CONFIGS));
    
    public static <T extends Record> Config<T> register(ConfigType type, String id, T defaults) {
        var group = LOOKUP.get(type);
        var existing = group.stream().filter(config -> config.id.equals(id)).findAny();

        if (existing.isPresent()) {
            if (existing.get().type == type && existing.get().defaults.equals(defaults))
                // Just checked the actual type
                //noinspection unchecked
                return (Config<T>) existing.get();
            throw new IllegalStateException("Config registered twice with different options!");
        }
        
        // All record classes are final so the class has to match
        //noinspection unchecked
        var config = new Config<>((Class<T>) defaults.getClass(), id, type, defaults);
        group.add(config);
        return config;
    }
    
}
