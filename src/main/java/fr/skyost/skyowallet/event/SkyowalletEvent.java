package fr.skyost.skyowallet.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents a Skyowallet event that can be cancelled.
 */

public class SkyowalletEvent extends Event implements Cancellable {

	/**
	 * All registered handlers.
	 */
	
	private static final HandlerList HANDLERS = new HandlerList();

	/**
	 * Whether this event has been cancelled.
	 */

	private boolean isCancelled;

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(final boolean isCancelled) {
		this.isCancelled = isCancelled;
	}
	
	@Override
	public final HandlerList getHandlers() {
		return HANDLERS;
	}
	
	/**
	 * Returns the HandlerList.
	 * 
	 * @return The HandlerList.
	 */
	
	public static HandlerList getHandlerList() {
    	return HANDLERS;
    }

}