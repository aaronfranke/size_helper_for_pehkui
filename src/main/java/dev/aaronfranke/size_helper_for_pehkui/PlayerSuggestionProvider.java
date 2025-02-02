package dev.aaronfranke.size_helper_for_pehkui;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

// Directly copied from the FabricMC documentation.
// https://docs.fabricmc.net/develop/commands/suggestions#creating-a-custom-suggestion-provider
public class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		Collection<String> playerNames = source.getPlayerNames();
		for (String playerName : playerNames) {
			builder.suggest(playerName);
		}
		return builder.buildFuture();
	}
}
