package fr.skyost.skyowallet.extensions;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.utils.Skyoconfig;
import fr.skyost.skyowallet.utils.Utils;

public class Mine4Cash extends SkyowalletExtension {
	
	private ExtensionConfig config;
	
	public Mine4Cash(final Plugin plugin) throws InvalidConfigurationException {
		super(plugin);
	}
	
	@Override
	public final String getName() {
		return "Mine4Cash";
	}

	@Override
	public final HashMap<String, PermissionDefault> getPermissions() {
		final HashMap<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("mine4cash.earn", PermissionDefault.TRUE);
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
	private final void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final String rawReward = config.data.get(block.getType().name());
		if(rawReward != null) {
			final Double reward = Utils.doubleTryParse(rawReward);
			if(reward != null) {
				final Player player = event.getPlayer();
				if(player.hasPermission("mine4cash.earn")) {
					final SkyowalletAccount account = SkyowalletAPI.getAccount(player.getUniqueId().toString());
					account.setWallet(account.getWallet() + reward);
					player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
					if(config.autoDropItem) {
						block.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	public class ExtensionConfig extends Skyoconfig {

		@ConfigOptions(name = "mine4cash.enable")
		public boolean enable = false;
		@ConfigOptions(name = "mine4cash.data")
		public HashMap<String, String> data = new HashMap<String, String>();
		@ConfigOptions(name = "mine4cash.auto-drop-item")
		public boolean autoDropItem = false;
		
		private ExtensionConfig(final File file) {
			super(file, Arrays.asList("Mine4Cash Configuration"));
			data.put(Material.GOLD_ORE.name(), "100.0");
			data.put(Material.DIAMOND_ORE.name(), "150.0");
			data.put(Material.EMERALD_ORE.name(), "200.0");
		}
		
	}

}
