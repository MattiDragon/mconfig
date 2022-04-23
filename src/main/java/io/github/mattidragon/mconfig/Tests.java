package io.github.mattidragon.mconfig;

import io.github.mattidragon.mconfig.config.ConfigManager;
import io.github.mattidragon.mconfig.config.ConfigType;
import net.fabricmc.fabric.api.util.TriState;

public class Tests {
    public static void register() {
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-simple", new SimpleConfig("Hello\n==", 32, true)).get());
        System.out.println(ConfigManager.register(ConfigType.CLIENT, "mconfgi_test-simple", new SimpleConfig("Hellö\n==", 46, false)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-simple", new SimpleConfig("Héllo\n==", 67, true)).get());
    
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-complex", new ComplexConfig((short) 8999, 0.3, "bbbbb", TriState.TRUE)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-complex", new ComplexConfig((short) 89, 2.5, "aaaa", TriState.DEFAULT)).get());
    }
    
    public record SimpleConfig(String thingName, int thingSpeed, boolean thingEnabled) {};
    
    public record ComplexConfig(short shortBoy, double bigboy, String aaaa, TriState state) {};
}
