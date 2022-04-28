package io.github.mattidragon.mconfig.config;

import de.poiu.apron.PropertyFile;
import de.poiu.apron.entry.BasicEntry;
import io.github.mattidragon.mconfig.Mconfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class Config<T extends Record> {
    public final Class<T> clazz;
    public final String id;
    public final ConfigType type;
    public final T defaults;
    private @Nullable T value = null;
    
    Config(Class<T> clazz, String id, ConfigType type, T defaults) {
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
    
    /**
     * Reloads the config from disk and replaces any missing keys.
     * @return Whether the reload was successful. If not, an error will be logged.
     */
    public boolean load() {
        try {
            var configDir = FabricLoader.getInstance().getConfigDir().resolve(id);
            var file = (switch (type) {
                case COMMON -> configDir.resolve("common.properties");
                case CLIENT -> configDir.resolve("client.properties");
                case SERVER -> configDir.resolve("server.properties");
            }).toFile();
    
            if (file.exists()) {
                var properties = PropertyFile.from(file);
                if (!deserialize(properties)) return true;
            } else value = this.defaults;
    
            file.getParentFile().mkdirs();
            //noinspection ConstantConditions
            var properties = serialize(value);
            properties.saveTo(file);
        } catch (ConfigException e) {
            Mconfig.LOGGER.error("Config (re)loading failed for " + id + "/" + type.name().toLowerCase() + ", using defaults!", e);
            value = defaults;
            return false;
        } catch (RuntimeException e) {
            Mconfig.LOGGER.error("Config (re)loading failed for " + id + "/" + type.name().toLowerCase() + " due to an unexpected error, using defaults!", e);
            value = defaults;
            return false;
        }
        return true;
    }
    
    private PropertyFile serialize(Record config) {
        var clazz = config.getClass();
        var properties = new PropertyFile();
        
        for (var component : clazz.getRecordComponents()) {
            try {
                var comment = component.getAnnotation(Comment.class);
                if (comment != null) properties.appendEntry(new BasicEntry(Arrays.stream(comment.value().split("\\n"))
                        .map(s -> "# " + s)
                        .collect(Collectors.joining("\n", "", "\n"))));
                var componentValue = serializeField(component.getType(), component.getAccessor().invoke(config));
                
                properties.set(component.getName(), componentValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ConfigException("Error while serializing config!", e);
            }
        }
        
        return properties;
    }
    
    private boolean deserialize(PropertyFile config) {
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
                values.add(deserializeField(component.getType(), config.get(name), component.getName()));
            }
            
            // Safe because constructors produce objects of their class
            // noinspection unchecked
            value = (T) MethodHandles.lookup()
                    .findConstructor(clazz, MethodType.methodType(void.class, Arrays.stream(clazz.getRecordComponents()).map(RecordComponent::getType).toArray(Class[]::new)))
                    .invokeWithArguments(values);
        } catch (Throwable e) {
            throw new ConfigException("Error while deserializing config!", e);
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
    
        throw new ConfigException("Unsupported config field type " + clazz.getSimpleName() + "!");
    }
    
    @SuppressWarnings("unchecked") // Lots of casts based on class object checks. Should all be safe
    private <F> F deserializeField(Class<F> clazz, String value, String fieldName) {
        if (clazz == String.class) return (F) value;
        
        if (clazz == boolean.class || clazz == Boolean.class) {
            if ("true".equalsIgnoreCase(value)) return (F) Boolean.TRUE;
            if ("false".equalsIgnoreCase(value)) return (F) Boolean.FALSE;
            throw createException(fieldName, "a boolean", value);
        }
    
        try {
            if (clazz == byte.class || clazz == Byte.class) return (F) Byte.valueOf(value);
            if (clazz == short.class || clazz == Short.class) return (F) Short.valueOf(value);
            if (clazz == int.class || clazz == Integer.class) return (F) Integer.valueOf(value);
            if (clazz == long.class || clazz == Long.class) return (F) Long.valueOf(value);
            if (clazz == float.class || clazz == Float.class) return (F) Float.valueOf(value);
            if (clazz == double.class || clazz == Double.class) return (F) Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw createException(fieldName, "a valid " + clazz.getSimpleName().toLowerCase(), value);
        }
        
        if (clazz == Identifier.class) {
            var id = Identifier.tryParse(fieldName);
            if (id == null) throw createException(fieldName, "a valid identifier", value);
            return (F) id;
        }
        
        if (clazz.isEnum()) {
            for (F constant : clazz.getEnumConstants()) {
                if (((Enum<?>) constant).name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
            throw createException(fieldName, getEnumValuesAsString(clazz).toString(), value);
        }
        
        throw new ConfigException("Unsupported config field type " + clazz.getSimpleName() + "!");
    }
    
    private ConfigException createException(String fieldName, String type, String value) {
        return new ConfigException("Expected '" + fieldName + "' to be " + type + ", but found '" + value + "' instead!");
    }
    
    @NotNull
    private <F> StringBuilder getEnumValuesAsString(Class<F> clazz) {
        var error = new StringBuilder();
        var enumConstants = (Enum<?>[]) clazz.getEnumConstants();
        var length = enumConstants.length;
        for (int i = 0; i < length; i++) {
            error.append(enumConstants[i].name());
            if (i == length - 1)
                continue;
            if (i == length - 2) error.append(" or ");
            else error.append(", ");
        }
        return error;
    }
}
