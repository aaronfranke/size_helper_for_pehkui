package dev.aaronfranke.size_helper_for_pehkui.mixin;

import dev.aaronfranke.size_helper_for_pehkui.PlayerStruckByLightningCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "onStruckByLightning", at = @At("HEAD"))
	private void onStruckByLightningMixin(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
		if ((Object) this instanceof PlayerEntity player) {
			PlayerStruckByLightningCallback.EVENT.invoker().onPlayerStruckByLightning(player, lightning);
		}
	}
}
