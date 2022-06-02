package io.github.mattidragon.mconfig.internal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.mattidragon.mconfig.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class MconfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("mconfig-client")
                    .then(ClientCommandManager.literal("reload")
                            .then(configArgument("id")
                                    .executes(context -> {
                                        var config = ConfigManager.CLIENT_CONFIGS.stream()
                                                .filter(config1 -> config1.id.equals(StringArgumentType.getString(context, "id")))
                                                .findAny()
                                                .orElse(null);
                                
                                        if (config == null) {
                                            context.getSource().sendError(Text.translatable("mconfig.unknown_config"));
                                            return 0;
                                        }
                                        try {
                                            config.load();
                                        } catch (RuntimeException e) {
                                            context.getSource().sendError(Text.translatable("mconfig.reload_fail"));
                                            Mconfig.LOGGER.error("Config reload failed!", e);
                                            return 0;
                                        }
                                        context.getSource().sendFeedback(Text.translatable("mconfig.reload_success"));
                                        return 1;
                                    }))));
    
        }));
        
    
    }
    
    private RequiredArgumentBuilder<FabricClientCommandSource, String> configArgument(String id) {
        return ClientCommandManager.argument(id, StringArgumentType.word())
                .suggests((context, builder) -> {
                    ConfigManager.CLIENT_CONFIGS.forEach(config -> builder.suggest(config.id));
                    return builder.buildFuture();
                });
    }
}
