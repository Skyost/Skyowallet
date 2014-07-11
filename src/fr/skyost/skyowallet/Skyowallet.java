package fr.skyost.skyowallet;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.commands.SkyowalletCommand;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.commands.subcommands.*;
import fr.skyost.skyowallet.events.GlobalEvents;
import fr.skyost.skyowallet.extensions.CommandsCosts;
import fr.skyost.skyowallet.extensions.Mine4Cash;
import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.MetricsLite;
import fr.skyost.skyowallet.utils.Skyupdater;

public class Skyowallet extends JavaPlugin {
	
	protected static PluginConfig config;
	public static PluginMessages messages;
	
	@Override
	public final void onEnable() {
		try {
			final File dataFolder = this.getDataFolder();
			config = new PluginConfig(dataFolder);
			config.load();
			messages = new PluginMessages(dataFolder);
			messages.load();
			if(config.autoSyncInterval >= 1) {
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SyncTask(), 0, config.autoSyncInterval * 20L);
			}
			final SkyowalletCommand skyowalletCmd = new SkyowalletCommand();
			for(final CommandInterface command : new CommandInterface[]{new SkyowalletInfos(), new SkyowalletPay(), new SkyowalletSet(), new SkyowalletSync(), new SkyowalletView()}) {
				skyowalletCmd.registerSubCommand(command);
			}
			final PluginCommand command = this.getCommand("skyowallet");
			command.setUsage(ChatColor.RED + command.getUsage());
			command.setExecutor(skyowalletCmd);
			final PluginManager manager = Bukkit.getPluginManager();
			manager.registerEvents(new GlobalEvents(), this);
			if(config.enableUpdater) {
				new Skyupdater(this, 82182, this.getFile(), true, true);
			}
			if(config.enableMetrics) {
				new MetricsLite(this).start();
			}
			if(config.warnOfflineMode && !Bukkit.getOnlineMode()) {
				final ConsoleCommandSender console = Bukkit.getConsoleSender();
				console.sendMessage(ChatColor.RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				console.sendMessage(ChatColor.RED + "!!                                                  !!");
				console.sendMessage(ChatColor.RED + "!!                /!\\ WARNING /!\\                   !!");
				console.sendMessage(ChatColor.RED + "!!                                                  !!");
				console.sendMessage(ChatColor.RED + "!!     Your server seems to be in offline mode,     !!");
				console.sendMessage(ChatColor.RED + "!!  Therefore, some problems can occur with UUIDs.  !!");
				console.sendMessage(ChatColor.RED + "!!      Please do not send me a ticket. Thanks,     !!");
				console.sendMessage(ChatColor.RED + "!!                   - Skyost                       !!");
				console.sendMessage(ChatColor.RED + "!!                                                  !!");
				console.sendMessage(ChatColor.RED + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			final Logger logger = this.getLogger();
			if(config.mine4CashEnable) {
				logger.log(Level.INFO, "Enabling Mine4Cash...");
				final HashMap<Material, Double> data = new HashMap<Material, Double>();
				for(final Entry<String, String> entry : config.mine4CashData.entrySet()) {
					data.put(Material.valueOf(entry.getKey()), Double.parseDouble(entry.getValue()));
				}
				final Mine4Cash extension = new Mine4Cash(this, data, config.mine4CashAutoDropItem);
				for(final Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
					manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
				}
				logger.log(Level.INFO, "Mine4Cash enabled !");
			}
			if(config.commandsCostsEnable) {
				logger.log(Level.INFO, "Enabling CommandsCosts...");
				final HashMap<String, Double> data = new HashMap<String, Double>();
				for(final Entry<String, String> entry : config.commandsCostsData.entrySet()) {
					data.put(entry.getKey(), Double.parseDouble(entry.getValue()));
				}
				final CommandsCosts extension = new CommandsCosts(this, data);
				for(final Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
					manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
				}
				logger.log(Level.INFO, "CommandsCosts enabled !");
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	
	@Override
	public final void onDisable() {
		try {
			new SyncTask().run();
			if(SkyowalletAPI.statement != null && !SkyowalletAPI.statement.isClosed()) {
				SkyowalletAPI.statement.close();
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}

}
