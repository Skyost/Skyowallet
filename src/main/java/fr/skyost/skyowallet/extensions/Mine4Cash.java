package fr.skyost.skyowallet.extensions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.utils.Utils;

public class Mine4Cash extends SkyowalletExtension {
	
	private ExtensionConfig config;
	
	public Mine4Cash(final JavaPlugin plugin) {
		super(plugin, "Gives money to a player if he mines a specific block.");
	}
	
	@Override
	public final String getName() {
		return "Mine4Cash";
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
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
	private final void onBlockBreak(final BlockBreakEvent event) {
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
		final Player player = event.getPlayer();
		if(!player.hasPermission("mine4cash.earn") || !SkyowalletAPI.hasAccount(player)) {
			return;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		account.setWallet(account.getWallet() + reward);
		player.getWorld().playSound(player.getLocation(), config.sound, 1f, 1f);
		if(config.autoDropItem) {
			block.setType(Material.AIR);
		}
	}
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {

		@ConfigOptions(name = "rewards")
		public HashMap<String, String> rewards = new HashMap<String, String>();
		@ConfigOptions(name = "auto-drop-item")
		public boolean autoDropItem = false;
		@ConfigOptions(name = "sound")
		public Sound sound = Sound.ENTITY_EXPERIENCE_ORB_PICKUP;
		
		private ExtensionConfig() {
			super();
			
			rewards.put(Material.GOLD_ORE.name(), "100.0");
			rewards.put(Material.DIAMOND_ORE.name(), "150.0");
			rewards.put(Material.EMERALD_ORE.name(), "200.0");
		}
		
	}

}