package io.github.mattidragon.mconfig;

import io.github.mattidragon.mconfig.config.Comment;
import io.github.mattidragon.mconfig.config.ConfigManager;
import io.github.mattidragon.mconfig.config.ConfigType;
import net.fabricmc.fabric.api.util.TriState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Tests {
    public static void register() {
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-simple", new SimpleConfig("Hello\n==", 32, true)).get());
        System.out.println(ConfigManager.register(ConfigType.CLIENT, "mconfgi_test-simple", new SimpleConfig("Hellö\n==", 46, false)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-simple", new SimpleConfig("Héllo\n==", 67, true)).get());
    
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-complex", new ComplexConfig((short) 8999, 0.3, "bbbbb", TriState.TRUE)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-complex", new ComplexConfig((short) 89, 2.5, "aaaa", TriState.DEFAULT)).get());
    }
    
    public record SimpleConfig(@Comment("Testing") String thingName, int thingSpeed, boolean thingEnabled) {}
    
    public record ComplexConfig(short shortBoy, double bigboy, @Comment("very \n\n mush") String aaaa, TriState state) {}
}
