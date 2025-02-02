package dev.aaronfranke.size_helper_for_pehkui;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.CompletableFuture;

public class ScaleSettingSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
	// Keep this in sync with the cases in ScaleSettings.setScaleSetting.
	private static final String[] scaleSettingExposedNames = {
		"height_meters",
		"baked_height_meters",
		"fatness",
		"jumping",
		"strength",
		"eye_height",
		"hitbox_height",
		"hitbox_width",
		"motion",
		"third_person_distance",
	};

	@Override
	public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		for (String scaleSettingName : scaleSettingExposedNames) {
			builder.suggest(scaleSettingName);
		}
		return builder.buildFuture();
	}
}
