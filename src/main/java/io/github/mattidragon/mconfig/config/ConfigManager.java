package io.github.mattidragon.mconfig.config;

import com.google.common.collect.*;

import java.util.*;

public final class ConfigManager {
    public static final List<Config<?>> CLIENT_CONFIGS;
    public static final List<Config<?>> COMMON_CONFIGS;
    public static final List<Config<?>> SERVER_CONFIGS;
    private static final ImmutableMap<ConfigType, List<Config<?>>> LOOKUP;
    
    private ConfigManager(){}
    
    public static <T extends Record> Config<T> register(ConfigType type, String id, T defaults) {
        var group = LOOKUP.get(type);
        var existing = group.stream().filter(config -> config.id.equals(id)).findAny();

        if (existing.isPresent()) {
            if (existing.get().type == type && existing.get().defaults.equals(defaults))
                // Just checked the actual type
                //noinspection unchecked
                return (Config<T>) existing.get();
            throw new IllegalStateException("Config registered twice with different options for same type!");
        }
        
        // All record classes are final so the class has to match
        //noinspection unchecked
        var config = new Config<>((Class<T>) defaults.getClass(), id, type, defaults);
        group.add(config);
        return config;
    }
    
    static {
        var client = new ArrayList<Config<?>>();
        CLIENT_CONFIGS = Collections.unmodifiableList(client);
        var common = new ArrayList<Config<?>>();
        COMMON_CONFIGS = Collections.unmodifiableList(common);
        var server = new ArrayList<Config<?>>();
        SERVER_CONFIGS = Collections.unmodifiableList(server);
        LOOKUP = Maps.immutableEnumMap(Map.of(ConfigType.COMMON, common, ConfigType.SERVER, server, ConfigType.CLIENT, client));
    }
}
