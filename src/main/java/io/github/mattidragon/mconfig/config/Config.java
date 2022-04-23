package io.github.mattidragon.mconfig.config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Config<T extends Record> {
    public final Class<T> clazz;
    public final String id;
    public final ConfigType type;
    public final T defaults;
    private @Nullable T value = null;
    
    public Config(Class<T> clazz, String id, ConfigType type, T defaults) {
        this.clazz = clazz;
        this.id = id;
        this.type = type;
        this.defaults = defaults;
    }
    
    public T get() {
        if (value == null) {
            load();
        }
        return value;
    }
    
    public void load() {
        var configDir = FabricLoader.getInstance().getConfigDir().resolve(id);
        Path path = switch (type) {
            case COMMON -> configDir.resolve("common.properties");
            case CLIENT -> configDir.resolve("client.properties");
            case SERVER -> configDir.resolve("server.properties");
        };
        
        if (Files.exists(path)) {
            try {
                var properties = new Properties();
                properties.load(Files.newBufferedReader(path));
                if (!deserialize(properties)) return;
            } catch (IOException e) {
                throw new RuntimeException("Error while reading config file!", e);
            }
        } else value = defaults;
        
        try {
            Files.createDirectories(path.getParent());
            //noinspection ConstantConditions // not null!
            var properties = serialize(value);
            properties.store(Files.newBufferedWriter(path), null);
        } catch (IOException e) {
            throw new RuntimeException("Error while writing default config file!", e);
        }
    }
    
    private Properties serialize(Record config) {
        var clazz = config.getClass();
        var properties = new Properties();
        
        for (var component : clazz.getRecordComponents()) {
            try {
                properties.put(component.getName(), serializeField(component.getType(), component.getAccessor().invoke(config)));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Error while serializing config!", e);
            }
        }
        
        return properties;
    }
    
    private boolean deserialize(Properties config) {
        boolean needsUpdate = false;
        var values = new ArrayList<>();
        try {
            for (var component : clazz.getRecordComponents()) {
                var name = component.getName();
                if (!config.containsKey(name)) {
                    needsUpdate = true;
                    values.add(component.getAccessor().invoke(defaults));
                    continue;
                }
                values.add(deserializeField(component.getType(), config.getProperty(name)));
            }
            
            
            // Safe because constructors produce objects of their class
            // noinspection unchecked
            value = (T) MethodHandles.lookup()
                    .findConstructor(clazz, MethodType.methodType(void.class, Arrays.stream(clazz.getRecordComponents()).map(RecordComponent::getType).toArray(Class[]::new)))
                    .invokeWithArguments(values);
        } catch (Throwable e) {
            throw new RuntimeException("Error while deserializing config!", e);
        }
        return needsUpdate;
    }
    
    private <F> String serializeField(Class<F> clazz, Object value) {
        if (clazz == boolean.class || clazz == Boolean.class) return Boolean.toString((Boolean) value);
        if (clazz == byte.class || clazz == Byte.class) return Byte.toString((Byte) value);
        if (clazz == short.class || clazz == Short.class) return Short.toString((Short) value);
        if (clazz == int.class || clazz == Integer.class) return Integer.toString((Integer) value);
        if (clazz == long.class || clazz == Long.class) return Long.toString((Long) value);
        if (clazz == float.class || clazz == Float.class) return Float.toString((Float) value);
        if (clazz == double.class || clazz == Double.class) return Double.toString((Double) value);
    
        if (clazz == String.class) return (String) value;
    
        if (clazz.isEnum()) return ((Enum<?>)value).name();
    
        throw new IllegalStateException("Unexpected config field type!");
    }
    
    @SuppressWarnings("unchecked") // Lots of casts based on class object checks. Should all be safe
    private <F> F deserializeField(Class<F> clazz, String value) {
        if (clazz == boolean.class || clazz == Boolean.class) return (F) Boolean.valueOf(value);
        if (clazz == byte.class || clazz == Byte.class) return (F) Byte.valueOf(value);
        if (clazz == short.class || clazz == Short.class) return (F) Short.valueOf(value);
        if (clazz == int.class || clazz == Integer.class) return (F) Integer.valueOf(value);
        if (clazz == long.class || clazz == Long.class) return (F) Long.valueOf(value);
        if (clazz == float.class || clazz == Float.class) return (F) Float.valueOf(value);
        if (clazz == double.class || clazz == Double.class) return (F) Double.valueOf(value);
        
        
        if (clazz == String.class) return (F) value;
        
        if (clazz.isEnum()) return (F) Arrays.stream(clazz.getEnumConstants()).filter((constant) -> ((Enum<?>)constant).name().equalsIgnoreCase(value)).findFirst().orElseThrow(() -> new IllegalStateException("Unknown enum constant!"));
        
        throw new IllegalStateException("Unexpected config field type!");
    }
}
