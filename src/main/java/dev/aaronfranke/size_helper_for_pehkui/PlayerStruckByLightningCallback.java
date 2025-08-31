package dev.aaronfranke.size_helper_for_pehkui;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerStruckByLightningCallback {
	Event<PlayerStruckByLightningCallback> EVENT = EventFactory.createArrayBacked(PlayerStruckByLightningCallback.class, listeners -> (player, lightning) -> {
		for (PlayerStruckByLightningCallback listener : listeners) {
			listener.onPlayerStruckByLightning(player, lightning);
		}
	});

	void onPlayerStruckByLightning(PlayerEntity player, LightningEntity lightning);
}
