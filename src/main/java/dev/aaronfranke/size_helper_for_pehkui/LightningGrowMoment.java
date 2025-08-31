package dev.aaronfranke.size_helper_for_pehkui;

import net.minecraft.world.World;

public class LightningGrowMoment {
	private final World world;
	private final int growDurationTicks;
	private final long startingTick;
	private final long endingTick;

	public LightningGrowMoment(double growDurationSeconds, World world) {
		this.world = world;
		this.growDurationTicks = (int) Math.max(2, Math.round(growDurationSeconds * 20)); // 20 ticks per second
		this.startingTick = world.getTime();
		this.endingTick = startingTick + growDurationTicks;
	}

	public boolean isFinished() {
		return isTickAtOrMoreThanEnd(world.getTime());
	}

	public double getAdjustedScaleMultiplier() {
		final long currentTick = world.getTime();
		if (isTickAtOrMoreThanEnd(currentTick)) {
			return 0.0;
		}
		final long elapsed = currentTick - startingTick;
		final double fraction = (double) elapsed / (double) growDurationTicks;
		return Math.max(0.0, 1.0 - fraction);
	}

	private boolean isTickAtOrMoreThanEnd(long tick) {
		return tick >= endingTick;
	}
}
