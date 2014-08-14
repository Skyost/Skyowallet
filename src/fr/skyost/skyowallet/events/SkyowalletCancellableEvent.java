package fr.skyost.skyowallet.events;

import org.bukkit.event.Cancellable;

public class SkyowalletCancellableEvent extends SkyowalletEvent implements Cancellable {
	
	private boolean isCancelled;

	@Override
	public final boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public final void setCancelled(final boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

}
