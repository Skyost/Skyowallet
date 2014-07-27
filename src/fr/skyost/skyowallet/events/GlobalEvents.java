package fr.skyost.skyowallet.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import fr.skyost.skyowallet.SkyowalletAPI;

public class GlobalEvents implements Listener {
	
	@EventHandler
	private final void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		if(!SkyowalletAPI.hasAccount(player)) {
			SkyowalletAPI.registerAccount(player.getUniqueId());
		}
	}

}
