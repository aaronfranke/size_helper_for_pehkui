package dev.aaronfranke.size_helper_for_pehkui.suggestion_providers;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class EnumValueSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
		String enumSetting = StringArgumentType.getString(context, "enum_setting");
		if (enumSetting.equals("flight")) {
			builder.suggest("none");
			builder.suggest("creative");
			builder.suggest("elytra");
		}
		return builder.buildFuture();
	}
}
