package fr.skyost.skyowallet.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkyowalletEvent extends Event {
	
private static final HandlerList HANDLERS = new HandlerList();
	
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

}
