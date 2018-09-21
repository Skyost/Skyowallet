package fr.skyost.skyowallet.extension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.util.Utils;

/**
 * Mine4Cash extension class.
 */

public class Mine4Cash extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * Creates a new mine for cash instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public Mine4Cash(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "Gives money to a player if he mines a specific block.");
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<>();
		permissions.put("mine4cash.earn", PermissionDefault.TRUE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@Override
	public final String getFileName() {
		return "mine4cash.yml";
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onBlockBreak(final BlockBreakEvent event) {
		if(event.isCancelled()) {
			return;
		}
		final Block block = event.getBlock();
		final String rawReward = config.rewards.get(block.getType().name());
		if(rawReward == null) {
			return;
		}
		final Double reward = Utils.doubleTryParse(rawReward);
		if(reward == null) {
			return;
		}

		final SkyowalletAccountManager accountManager = getSkyowallet().getAccountManager();
		final Player player = event.getPlayer();
		if(!player.hasPermission("mine4cash.earn") || !accountManager.has(player)) {
			return;
		}
		final WalletHolder wallet = accountManager.get(player).getWallet();
		wallet.addAmount(reward);
		player.getWorld().playSound(player.getLocation(), config.sound, 1f, 1f);
		if(config.autoDropItem) {
			block.setType(Material.AIR);
		}
	}

	/**
	 * Represents the extension configuration.
	 */
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {

		@ConfigOptions(name = "rewards")
		public HashMap<String, String> rewards = new HashMap<>();
		@ConfigOptions(name = "auto-drop-item")
		public boolean autoDropItem = false;
		@ConfigOptions(name = "sound")
		public Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;

		/**
		 * Creates a new extension configuration instance.
		 */
		
		private ExtensionConfig() {
			super();
			
			rewards.put(Material.GOLD_ORE.name(), "100.0");
			rewards.put(Material.DIAMOND_ORE.name(), "150.0");
			rewards.put(Material.EMERALD_ORE.name(), "200.0");
		}
		
	}

}