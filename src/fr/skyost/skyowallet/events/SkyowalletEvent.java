package fr.skyost.skyowallet.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkyowalletEvent extends Event implements Cancellable {
	
	private static final HandlerList HANDLERS = new HandlerList();
	
	private boolean isCancelled;

	@Override
	public final HandlerList getHandlers() {
		return HANDLERS;
	}
	
	/**
	 * Gets the HandlerList.
	 * 
	 * @return The HandlerList.
	 */
	
	public static final HandlerList getHandlerList() {
    	return HANDLERS;
    }

	@Override
	public final boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public final void setCancelled(final boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

}
