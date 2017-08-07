package fr.skyost.skyowallet.extensions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.utils.Utils;

public class CommandsCosts extends SkyowalletExtension {
	
	private ExtensionConfig config;
	
	public CommandsCosts(final JavaPlugin plugin) {
		super(plugin, "It costs money to a player if he wants to use a command.");
	}
	
	@Override
	public final String getName() {
		return "CommandsCosts";
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("commandscosts.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private final void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		if(!player.hasPermission("commandscosts.bypass")) {
			return;
		}
		final String rawCost = config.commands.get(event.getMessage().substring(1).split(" ")[0]);
		if(rawCost == null) {
			return;
		}
		final Double cost = Utils.doubleTryParse(rawCost);
		if(cost == null) {
			return;
		}
		if(!SkyowalletAPI.hasAccount(player)) {
			player.sendMessage(Skyowallet.messages.message33);
			return;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		final double wallet = account.getWallet() - cost;
		if(wallet < 0.0) {
			player.sendMessage(config.message1.replace("/cost/", String.valueOf(cost)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(cost)));
			event.setCancelled(true);
			return;
		}
		account.setWallet(wallet);
	}
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "commands")
		public HashMap<String, String> commands = new HashMap<String, String>();
		
		@ConfigOptions(name = "messages.1")
		public String message1 = ChatColor.RED + "You do not have enough money to run that command. Cost : /cost/ /currency-name/.";
		
		private ExtensionConfig() {
			super();
			commands.put("pl", "10.0");
		}
		
	}

}