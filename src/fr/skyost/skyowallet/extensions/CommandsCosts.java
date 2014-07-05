package fr.skyost.skyowallet.extensions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;

public class CommandsCosts implements SkyowalletExtension, Listener {
	
	private final HashMap<String, Double> commands;
	
	public CommandsCosts(final Plugin plugin, final HashMap<String, Double> commands) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
		this.commands = commands;
	}
	
	@Override
	public final String name() {
		return "CommandsCosts";
	}

	@Override
	public final HashMap<String, PermissionDefault> permissions() {
		final HashMap<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("commandscosts.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@EventHandler
	private final void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event) {
		final Double cost = commands.get(event.getMessage().substring(1).split(" ")[0]);
		if(cost != null) {
			final Player player = event.getPlayer();
			if(!player.hasPermission("commandscosts.bypass")) {
				final SkyowalletAccount account = SkyowalletAPI.getAccount(player.getUniqueId().toString());
				final double wallet = account.getWallet() - cost;
				if(wallet < 0.0) {
					player.sendMessage(ChatColor.RED + "You do not have enough money to run that command.\nCost : " + cost);
					event.setCancelled(true);
					return;
				}
				account.setWallet(wallet);
			}
		}
	}

}
