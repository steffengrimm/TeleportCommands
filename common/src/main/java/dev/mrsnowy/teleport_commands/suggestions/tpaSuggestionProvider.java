package dev.mrsnowy.teleport_commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mrsnowy.teleport_commands.Constants;
import dev.mrsnowy.teleport_commands.commands.tpa;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class tpaSuggestionProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();

            tpa.tpaRequestMap.keySet().stream()
                    .filter(pair -> Objects.equals(player.getStringUUID(), pair.getValue()))
                    .forEach(pair -> {
                        String initPlayer = pair.getKey();
                        Optional<String> recPlayerName = Optional.ofNullable(
                                context.getSource().getServer().getPlayerList().getPlayer(UUID.fromString(initPlayer)))
                                .map(p -> p.getName().getString());
                        if (recPlayerName.isPresent()) {
                            builder.suggest(recPlayerName.get());
                        }
                    });

            // Build and return the suggestions
            return builder.buildFuture();
        } catch (Exception e) {
            Constants.LOGGER.error("Error getting tpa suggestions! ", e);
            return null;
        }
    }
}
