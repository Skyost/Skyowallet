package fr.skyost.skyowallet.extensions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;

public class Mine4Cash extends SkyowalletExtension {
	
	private final HashMap<Material, Double> items;
	private final boolean autoDropItem;
	
	public Mine4Cash(final Plugin plugin, final HashMap<Material, Double> items, final boolean autoDropItem) {
		this.items = items;
		this.autoDropItem = autoDropItem;
		Bukkit.getPluginManager().registerEvents(this, plugin);
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
	
	@EventHandler
	private final void onBlockBreak(final BlockBreakEvent event) {
		final Block block = event.getBlock();
		final Double reward = items.get(block.getType());
		if(reward != null) {
			final Player player = event.getPlayer();
			if(player.hasPermission("mine4cash.earn")) {
				final SkyowalletAccount account = SkyowalletAPI.getAccount(player.getUniqueId().toString());
				account.setWallet(account.getWallet() + reward);
				player.getWorld().playSound(player.getLocation(), Sound.ORB_PICKUP, 1f, 1f);
				if(autoDropItem) {
					block.setType(Material.AIR);
				}
			}
		}
	}

}
