package fr.skyost.skyowallet.extensions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.SkyowalletAPI;

public class GoodbyeWallet extends SkyowalletExtension {
	
	private ExtensionConfig config;
	private final Set<UUID> players = new HashSet<UUID>();
	
	public GoodbyeWallet(final JavaPlugin plugin) {
		super(plugin, "When a player dies, he loses his wallet.");
	}
	
	@Override
	public final String getName() {
		return "GoodbyeWallet";
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("goodbyewallet.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler
	private final void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		if(!player.hasPermission("goodbyewallet.bypass") && SkyowalletAPI.hasAccount(player)) {
			SkyowalletAPI.getAccount(player).setWallet(0.0);
			players.add(player.getUniqueId());
		}
	}
	
	@EventHandler
	private final void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		if(players.contains(uuid)) {
			player.sendMessage(config.message1);
			players.remove(uuid);
		}
	}
	
	@EventHandler
	private final void onPlayerQuit(final PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		if(players.contains(uuid)) {
			players.remove(uuid);
		}
	}
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "messages.1")
		public String message1 = ChatColor.DARK_RED + "You have lost your wallet !";
		
	}

}