package fr.skyost.skyowallet.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.utils.Skyoconfig;

public class GoodbyeWallet extends SkyowalletExtension {
	
	private ExtensionConfig config;
	private final List<UUID> players = new ArrayList<UUID>();
	
	public GoodbyeWallet(final Plugin plugin) {
		super(plugin);
	}
	
	@Override
	public final String getName() {
		return "GoodbyeWallet";
	}

	@Override
	public final HashMap<String, PermissionDefault> getPermissions() {
		final HashMap<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("goodbyewallet.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@Override
	public final Skyoconfig getConfiguration() {
		if(config == null) {
			config = new ExtensionConfig(this.getConfigurationFile());
		}
		return config;
	}
	
	@Override
	public final boolean isEnabled() {
		return config.enable;
	}
	
	@EventHandler
	private final void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		if(!player.hasPermission("goodbyewallet.bypass")) {
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
	
	public class ExtensionConfig extends Skyoconfig {
		
		@ConfigOptions(name = "goodbye-wallet.enable")
		public boolean enable = false;
		
		@ConfigOptions(name = "messages.1")
		public String message1 = ChatColor.DARK_RED + "You have lost your wallet !";
		
		private ExtensionConfig(final File file) {
			super(file, Arrays.asList("GoodbyeWallet Configuration"));
		}
		
	}

}
