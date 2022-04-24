package io.github.mattidragon.mconfig;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.mattidragon.mconfig.config.ConfigManager;
import io.github.mattidragon.mconfig.config.ConfigType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class MconfigClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("mconfig-client")
                .then(ClientCommandManager.literal("reload")
                                .then(configArgument("id")
                                        .executes(context -> {
                                            var config = ConfigManager.CLIENT_CONFIGS.stream()
                                                    .filter(config1 -> config1.id.equals(StringArgumentType.getString(context, "id")))
                                                    .findAny()
                                                    .orElse(null);
                                        
                                            if (config == null) {
                                                context.getSource().sendError(new TranslatableText("mconfig.unknown_config"));
                                                return 0;
                                            }
                                            try {
                                                config.load();
                                            } catch (RuntimeException e) {
                                                context.getSource().sendError(new TranslatableText("mconfig.reload_fail"));
                                                Mconfig.LOGGER.error("Config reload failed!", e);
                                                return 0;
                                            }
                                            context.getSource().sendFeedback(new TranslatableText("mconfig.reload_success"));
                                            return 1;
                                        }))));
    
    }
    
    private RequiredArgumentBuilder<FabricClientCommandSource, String> configArgument(String id) {
        return ClientCommandManager.argument(id, StringArgumentType.word())
                .suggests((context, builder) -> {
                    ConfigManager.CLIENT_CONFIGS.forEach(config -> builder.suggest(config.id));
                    return builder.buildFuture();
                });
    }
}
