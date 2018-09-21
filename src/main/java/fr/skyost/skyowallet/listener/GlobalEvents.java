package fr.skyost.skyowallet.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.skyost.skyowallet.Skyowallet;

/**
 * Represents all non-specific bukkit events listened by the plugin.
 */

public class GlobalEvents implements Listener {

	/**
	 * The Skyowallet instance.
	 */

	private final Skyowallet skyowallet;

	/**
	 * Creates a new global events instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public GlobalEvents(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if(!skyowallet.getAccountManager().has(player)) {
			skyowallet.getAccountManager().add(player);
		}
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

}