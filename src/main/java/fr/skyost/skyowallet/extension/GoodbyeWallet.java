package fr.skyost.skyowallet.extension;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;

/**
 * GoodbyeWallet extension class.
 */

public class GoodbyeWallet extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * All players who are dead and are waiting to lose their wallet.
	 */

	private final Set<UUID> players = new HashSet<>();

	/**
	 * Creates a new goodbye wallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public GoodbyeWallet(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "When a player dies, he loses his wallet.");
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<>();
		permissions.put("goodbyewallet.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler
	private void onPlayerDeath(final PlayerDeathEvent event) {
		final Player player = event.getEntity();
		final SkyowalletAccountManager accountManager = getSkyowallet().getAccountManager();
		if(!player.hasPermission("goodbyewallet.bypass") && accountManager.has(player)) {
			accountManager.get(player).getWallet().setAmount(0d, 0d);
			players.add(player.getUniqueId());
		}
	}
	
	@EventHandler
	private void onPlayerRespawn(final PlayerRespawnEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		if(players.contains(uuid)) {
			player.sendMessage(config.messageWalletLost);
			players.remove(uuid);
		}
	}
	
	@EventHandler
	private void onPlayerQuit(final PlayerQuitEvent event) {
		players.remove(event.getPlayer().getUniqueId());
	}

	/**
	 * Represents the extension configuration.
	 */

	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "messages.wallet-lost")
		public String messageWalletLost = ChatColor.DARK_RED + "You have lost your wallet !";
		
	}

}