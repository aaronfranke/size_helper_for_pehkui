package dev.aaronfranke.size_helper_for_pehkui;

import java.time.Instant;
import java.time.Duration;

public class LightningGrowMoment {
	private final double growDurationSeconds;

	private final Instant startedAt;
	private final Instant doneAfter;

	public LightningGrowMoment(double growDurationSeconds) {
		this.growDurationSeconds = growDurationSeconds > 0 ? growDurationSeconds : 0.1;
		startedAt = Instant.now();
		doneAfter = startedAt.plusMillis((long) growDurationSeconds * 1000);
	}

	public boolean getIsDone() {
		return Instant.now().isAfter(doneAfter);
	}

	public double getAdjustedScaleMultiplier() {
		final Instant now = Instant.now();
		if (now.isAfter(doneAfter)) {
			return 0.0;
		}
		final double elapsed = Duration.between(startedAt, now).toMillis() / 1000.0;
		final double fraction = elapsed / growDurationSeconds;
		return Math.max(0.0, 1.0 - fraction);
	}
}
