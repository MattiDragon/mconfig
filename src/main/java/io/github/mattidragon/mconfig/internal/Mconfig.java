package io.github.mattidragon.mconfig.internal;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.github.mattidragon.mconfig.config.Config;
import io.github.mattidragon.mconfig.config.ConfigManager;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public class Mconfig implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Mconfig.class);
    
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated) -> {
            dispatcher.register(literal("mconfig")
                    .then(literal("reload")
                            .then(literal("server")
                                    .then(configArgument("id", ConfigManager.SERVER_CONFIGS)
                                            .requires(Permissions.require("mconfig.reload", 3))
                                            .executes(context -> {
                                                var config = ConfigManager.SERVER_CONFIGS.stream()
                                                        .filter(config1 -> config1.id.equals(StringArgumentType.getString(context, "id")))
                                                        .findAny()
                                                        .orElse(null);
                                                
                                                if (config == null) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.unknown_config"));
                                                    return 0;
                                                }
    
                                                if (!config.reloadable) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.non_reloadable"));
                                                    return 0;
                                                }
                                                
                                                if (!config.load()) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.reload_fail"));
                                                    return 0;
                                                }
                                                
                                                context.getSource().sendFeedback(new TranslatableText("mconfig.reload_success"), true);
                                                return 1;
                                            })))
                            .then(literal("common")
                                    .then(configArgument("id", ConfigManager.COMMON_CONFIGS)
                                            .requires(Permissions.require("mconfig.reload", 3))
                                            .executes(context -> {
                                                var config = ConfigManager.COMMON_CONFIGS.stream()
                                                        .filter(config1 -> config1.id.equals(StringArgumentType.getString(context, "id")))
                                                        .findAny()
                                                        .orElse(null);
                                                
                                                if (config == null) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.unknown_config"));
                                                    return 0;
                                                }
                                                
                                                if (!config.reloadable) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.non_reloadable"));
                                                    return 0;
                                                }
                                                
                                                if (!config.load()) {
                                                    context.getSource().sendError(new TranslatableText("mconfig.reload_fail"));
                                                    return 0;
                                                }
                                                
                                                context.getSource().sendFeedback(new TranslatableText("mconfig.reload_success"), true);
                                                return 1;
                                            })))));
        }));
        //Tests.register();
    }
    
    private RequiredArgumentBuilder<ServerCommandSource, String> configArgument(String id, List<Config<?>> configList) {
        return CommandManager.argument(id, StringArgumentType.word())
                .suggests((context, builder) -> {
                    configList.forEach(config -> {
                        if (config.reloadable)
                            builder.suggest(config.id);
                    });
                    return builder.buildFuture();
                });
    }
}
