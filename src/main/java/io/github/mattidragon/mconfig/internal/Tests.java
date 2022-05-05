package io.github.mattidragon.mconfig.internal;

import io.github.mattidragon.mconfig.config.Comment;
import io.github.mattidragon.mconfig.config.ConfigManager;
import io.github.mattidragon.mconfig.config.ConfigType;
import io.github.mattidragon.mconfig.config.RegistryMember;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Tests {
    public static void register() {
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-simple", new SimpleConfig("Hello\n==", 32, true)).get());
        System.out.println(ConfigManager.register(ConfigType.CLIENT, "mconfgi_test-simple", new SimpleConfig("Hellö\n==", 46, false)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-simple", new SimpleConfig("Héllo\n==", 67, true)).get());
    
        System.out.println(ConfigManager.register(ConfigType.COMMON, "mconfgi_test-complex", new ComplexConfig((short) 8999, 0.3, "bbbbb", TriState.TRUE)).get());
        System.out.println(ConfigManager.register(ConfigType.SERVER, "mconfgi_test-complex", new ComplexConfig((short) 89, 2.5, "aaaa", TriState.DEFAULT)).get());
        
        var value = ConfigManager.register(ConfigType.SERVER, "mconfig_test-registry", new RegistryConfig(RegistryKey.of(Registry.BLOCK_KEY, new Identifier("oak_planks")), RegistryKey.of(Registry.ITEM_KEY, new Identifier("diamond")))).get();
        System.out.println(Registry.BLOCK.get(value.block));
        System.out.println(Registry.ITEM.get(value.item));
    }
    
    public record RegistryConfig(@RegistryMember("block") RegistryKey<Block> block, @RegistryMember("item") RegistryKey<Item> item) {}
    
    public record SimpleConfig(@Comment("Testing") String thingName, int thingSpeed, boolean thingEnabled) {}
    
    public record ComplexConfig(short shortBoy, double bigboy, @Comment("very \n\n mush") String aaaa, TriState state) {}
}
