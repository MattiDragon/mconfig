package io.github.mattidragon.mconfig;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.mattidragon.mconfig.config.ConfigManager;
import io.github.mattidragon.mconfig.config.ConfigType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
public class Mconfig implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Mconfig.class);
    
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("mconfig")
                    .then(CommandManager.literal("reload")
                                    .then(configArgument("id")
                                            .executes(context -> {
                                                var config = ConfigManager.SERVER_CONFIGS.stream()
                                                        .filter(config1 -> config1.id.equals(StringArgumentType.getString(context, "id")))
                                                        .findAny()
                                                        .orElse(null);
                                            
                                                if (config == null) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.unknown_config"));
                                                    return 0;
                                                }
                                                
                                                if (!config.load()) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.reload_fail"));
                                                    return 0;
                                                }
                                                context.getSource().sendFeedback(new TranslatableText("mconfig.reload_success"), true);
                                                return 1;
                                            }))));
        }));
        //Tests.register();
    }
    
    private RequiredArgumentBuilder<ServerCommandSource, String> configArgument(String id) {
        return CommandManager.argument(id, StringArgumentType.word())
                .suggests((context, builder) -> {
                    ConfigManager.SERVER_CONFIGS.forEach(config -> builder.suggest(config.id));
                    return builder.buildFuture();
                });
    }
}
