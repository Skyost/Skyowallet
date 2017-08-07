package fr.skyost.skyowallet.extensions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.utils.Utils;

public class KillerIncome extends SkyowalletExtension {
	
	private ExtensionConfig config;
	
	public KillerIncome(final JavaPlugin plugin) {
		super(plugin, "Win some money for killing an entity.");
	}
	
	@Override
	public final String getName() {
		return "KillerIncome";
	}
	
	@Override
	public final Map<String, PermissionDefault> getPermissions() {
		final Map<String, PermissionDefault> permissions = new HashMap<String, PermissionDefault>();
		permissions.put("killerincome.earn", PermissionDefault.TRUE);
		return permissions;
	}
	
	@Override
	public final SkyowalletExtensionConfig getConfiguration() {
		return config == null ? config = new ExtensionConfig() : config;
	}
	
	@EventHandler
	private final void onEntityDeath(final EntityDeathEvent event) {
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
		if(!SkyowalletAPI.hasAccount(killer)) {
			return;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(killer);
		account.setWallet(account.getWallet() + amount);
		final String entityName = type.name();
		killer.sendMessage(config.message1.replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)).replace("/entity/", entityName.charAt(0) + entityName.substring(1).toLowerCase()));
	}
	
	public class ExtensionConfig extends SkyowalletExtensionConfig {
		
		@ConfigOptions(name = "rewards")
		public LinkedHashMap<String, String> rewards = new LinkedHashMap<String, String>();
		
		@ConfigOptions(name = "messages.1")
		public String message1 = ChatColor.GOLD + "Congracubations ! You have won /amount/ /currency-name/ because you have killed a /entity/.";
		
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