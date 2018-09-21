package fr.skyost.skyowallet.extension;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.Utils;

/**
 * KillerIncome extension class.
 */

public class KillerIncome extends SkyowalletExtension {

	/**
	 * The extension configuration.
	 */
	
	private ExtensionConfig config;

	/**
	 * Creates a new killer income instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 */
	
	public KillerIncome(final Skyowallet skyowallet, final JavaPlugin plugin) {
		super(skyowallet, plugin, "Win some money for killing an entity.");
	}
	
	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<>();
		permissions.put("killerincome.earn", PermissionDefault.TRUE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler
	private void onEntityDeath(final EntityDeathEvent event) {
		final LivingEntity entity = event.getEntity();
		final EntityType type = entity.getType();
		final String rawAmount = config.rewards.get(type.name());
		if(rawAmount == null) {
			return;
		}
		final Double amount = Utils.doubleTryParse(rawAmount);
		if(amount == null) {
			return;
		}
		final Player killer = entity.getKiller();
		if(killer == null) {
			return;
		}

		final Skyowallet skyowallet = getSkyowallet();
		if(!skyowallet.getAccountManager().has(killer)) {
			return;
		}
		final WalletHolder wallet = skyowallet.getAccountManager().get(killer).getWallet();
		wallet.addAmount(amount);
		final String entityName = type.name();
		killer.sendMessage(PlaceholderFormatter.format(config.messageAmountWon, new PlaceholderFormatter.AmountPlaceholder(amount), new PlaceholderFormatter.CurrencyNamePlaceholder(amount), new PlaceholderFormatter.Placeholder("entity", entityName.charAt(0) + entityName.substring(1).toLowerCase())));
	}

	/**
	 * Represents the extension configuration.
	 */

	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "rewards")
		public LinkedHashMap<String, String> rewards = new LinkedHashMap<>();
		
		@ConfigOptions(name = "messages.amount-won")
		public String messageAmountWon = ChatColor.GOLD + "Congracubations ! You have won /amount/ /currency-name/ because you have killed a /entity/.";

		/**
		 * Creates a new extension configuration instance.
		 */

		private ExtensionConfig() {
			super();
			
			rewards.put(EntityType.CREEPER.name(), "10.0");
			rewards.put(EntityType.SPIDER.name(), "10.0");
			rewards.put(EntityType.ZOMBIE.name(), "12.5");
			rewards.put(EntityType.SKELETON.name(), "15.0");
			rewards.put(EntityType.ENDERMAN.name(), "17.5");
			rewards.put(EntityType.WITCH.name(), "20.0");
			rewards.put(EntityType.BLAZE.name(), "25.0");
			rewards.put(EntityType.PLAYER.name(), "30.0");
			rewards.put(EntityType.WITHER.name(), "50.0");
			rewards.put(EntityType.ENDER_DRAGON.name(), "100.0");
		}
		
	}

}