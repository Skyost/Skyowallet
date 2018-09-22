package fr.skyost.skyowallet.extension;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Utils;

/**
 * CommandCost extension class.
 */

public class CommandCost extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * Creates a new command cost instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public CommandCost(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "It costs money to a player if he wants to use a command.");
	}

	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<>();
		permissions.put("commandcost.bypass", PermissionDefault.FALSE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	private void onPlayerCommandPreprocessEvent(final PlayerCommandPreprocessEvent event) {
		if(event.isCancelled()) {
			return;
		}
		final Player player = event.getPlayer();
		if(player.hasPermission("commandcost.bypass")) {
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

		final Skyowallet skyowallet = getSkyowallet();
		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();
		if(!accountManager.has(player)) {
			player.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
			return;
		}
		final SkyowalletAccount account = accountManager.get(player);
		final WalletHolder wallet = account.getWallet();
		if(!wallet.canSubtract(cost)) {
			player.sendMessage(PlaceholderFormatter.format(config.messageNotEnoughMoney, new Placeholder("cost", String.valueOf(cost)), new CurrencyNamePlaceholder(cost)));
			event.setCancelled(true);
			return;
		}
		wallet.subtractAmount(cost);
	}

	/**
	 * Represents the extension configuration.
	 */
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "command")
		public HashMap<String, String> commands = new HashMap<>();
		
		@ConfigOptions(name = "messages.not-enough-money")
		public String messageNotEnoughMoney = ChatColor.RED + "You do not have enough money to run that command. Cost : /cost/ /currency-name/.";

		/**
		 * Creates a new extension configuration instance.
		 */

		private ExtensionConfig() {
			super();
			commands.put("pl", "10.0");
		}
		
	}

}