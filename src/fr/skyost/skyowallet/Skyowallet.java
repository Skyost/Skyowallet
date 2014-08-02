package fr.skyost.skyowallet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.commands.*;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.commands.subcommands.skyowallet.*;
import fr.skyost.skyowallet.commands.subcommands.bank.*;
import fr.skyost.skyowallet.extensions.*;
import fr.skyost.skyowallet.hooks.VaultHook;
import fr.skyost.skyowallet.listeners.GlobalEvents;
import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.MetricsLite;
import fr.skyost.skyowallet.utils.Skyupdater;

public class Skyowallet extends JavaPlugin {
	
	/**
	 * Skyowallet's config
	 */
	
	protected static PluginConfig config;
	
	/**
	 * Skyowallet's messages.
	 */
	
	public static PluginMessages messages;
	
	@Override
	public final void onEnable() {
		final PluginManager manager = Bukkit.getPluginManager();
		try {
			final File dataFolder = this.getDataFolder();
			config = new PluginConfig(dataFolder);
			config.load();
			messages = new PluginMessages(dataFolder);
			messages.load();
			final Runnable syncTask = new SyncTask();
			syncTask.run();
			if(config.autoSyncInterval > 0) {
				final long interval = config.autoSyncInterval * 20L;
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, syncTask, interval, interval);
			}
			final SkyowalletCommand skyowalletCmd = new SkyowalletCommand();
			for(final CommandInterface command : new CommandInterface[]{new SkyowalletInfos(), new SkyowalletPay(), new SkyowalletSet(), new SkyowalletSync(), new SkyowalletView()}) {
				skyowalletCmd.registerSubCommand(command);
			}
			final PluginCommand skyowallet = getCommand("skyowallet");
			skyowallet.setUsage("/" + skyowallet.getName() + " " + skyowalletCmd.getUsage());
			skyowallet.setExecutor(skyowalletCmd);
			final BankCommand bankCmd = new BankCommand();
			for(final CommandInterface command : new CommandInterface[]{new BankSetOwner(), new BankCreate(), new BankDelete(), new BankDeposit(), new BankInfos(), new BankJoin(), new BankLeave(), new BankList(), new BankRemoveOwner(), new BankWithdraw()}) {
				bankCmd.registerSubCommand(command);
			}
			final PluginCommand bank = getCommand("bank");
			bank.setUsage("/" + bank.getName() + " " + bankCmd.getUsage());
			bank.setExecutor(bankCmd);
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
			loadExtensions(manager, logger);
			final Plugin vault = manager.getPlugin("Vault");
			if(vault != null) {
				logger.log(Level.INFO, "Vault detected. Disabling all others plugins which use Vault...");
				final Plugin[] plugins = disableAllPlugins(manager, vault);
				logger.log(Level.INFO, "Hooking into Vault...");
				VaultHook.addToVault(vault);
				logger.log(Level.INFO, "Re-enabling the disabled plugins...");
				enableDisabledPlugins(manager, plugins);
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			manager.disablePlugin(this);
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
	
	/**
	 * Loads Skyowallet's extensions.
	 * 
	 * @param manager The plugin manager (used to register events).
	 * @param logger The logger (used to log events).
	 */
	
	private final void loadExtensions(final PluginManager manager, final Logger logger) {
		for(final SkyowalletExtension extension : new SkyowalletExtension[]{new Mine4Cash(this), new CommandsCosts(this), new GoodbyeWallet(this), new ScoreboardInfos(this)}) {
			final String name = extension.getName();
			try {
				extension.load();
				if(!extension.isEnabled()) {
					extension.disable();
					continue;
				}
				logger.log(Level.INFO, "Enabling " + name + "...");
				final HashMap<String, PermissionDefault> permissions = extension.getPermissions();
				if(permissions != null) {
					for(final Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
						manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
					}
				}
				logger.log(Level.INFO, name + " enabled !");
			}
			catch(final Exception ex) {
				logger.log(Level.SEVERE, "An error occured while enabling the extension \"" + name + "\" : " + ex.getClass().getName() + ".");
				continue;
			}
		}
	}
	
	/**
	 * Disable plugins which use Vault.
	 * 
	 * @param manager The plugin manager (used to disable plugins).
	 * @param vault Vault (because we do not want to disable it).
	 * 
	 * @return An array containing every plugins disabled.
	 */
	
	private final Plugin[] disableAllPlugins(final PluginManager manager, final Plugin vault) {
		final String name = vault.getName();
		final List<Plugin> plugins = new ArrayList<Plugin>();
		for(final Plugin plugin : manager.getPlugins()) {
			if(!plugin.equals(this) && !plugin.equals(vault) && plugin.isEnabled()) {
				final PluginDescriptionFile description = plugin.getDescription();
				if(description.getDepend().contains(name) || description.getSoftDepend().contains(name)) {
					manager.disablePlugin(plugin);
					plugins.add(plugin);
				}
			}
		}
		return plugins.toArray(new Plugin[plugins.size()]);
	}
	
	/**
	 * Enable disabled plugins.
	 * 
	 * @param manager The plugin manager (used to enable plugins).
	 * @param plugins The disabled plugins.
	 */
	
	private final void enableDisabledPlugins(final PluginManager manager, final Plugin[] plugins) {
		for(final Plugin plugin : plugins) {
			manager.enablePlugin(plugin);
		}
	}

}
