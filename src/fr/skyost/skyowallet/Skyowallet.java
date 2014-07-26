package fr.skyost.skyowallet;

import java.io.File;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.commands.*;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.commands.subcommands.skyowallet.*;
import fr.skyost.skyowallet.commands.subcommands.bank.*;
import fr.skyost.skyowallet.events.GlobalEvents;
import fr.skyost.skyowallet.extensions.CommandsCosts;
import fr.skyost.skyowallet.extensions.Mine4Cash;
import fr.skyost.skyowallet.extensions.SkyowalletExtension;
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
		try {
			final File dataFolder = this.getDataFolder();
			config = new PluginConfig(dataFolder);
			config.load();
			messages = new PluginMessages(dataFolder);
			messages.load();
			if(config.autoSyncInterval > 0) {
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SyncTask(), 0, config.autoSyncInterval * 20L);
			}
			final SkyowalletCommand skyowalletCmd = new SkyowalletCommand();
			for(final CommandInterface command : new CommandInterface[]{new SkyowalletInfos(), new SkyowalletPay(), new SkyowalletSet(), new SkyowalletSync(), new SkyowalletView()}) {
				skyowalletCmd.registerSubCommand(command);
			}
			this.getCommand("skyowallet").setExecutor(skyowalletCmd);
			final BankCommand bankCmd = new BankCommand();
			for(final CommandInterface command : new CommandInterface[]{new BankSetOwner(), new BankCreate(), new BankDelete(), new BankDeposit(), new BankInfos(), new BankJoin(), new BankLeave(), new BankList(), new BankRemoveOwner(), new BankWithdraw()}) {
				bankCmd.registerSubCommand(command);
			}
			this.getCommand("bank").setExecutor(bankCmd);
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
			for(final SkyowalletExtension extension : new SkyowalletExtension[]{new Mine4Cash(this), new CommandsCosts(this)}) {
				if(!extension.isEnabled()) {
					extension.disable();
					continue;
				}
				final String name = extension.getName();
				logger.log(Level.INFO, "Enabling " + name + "...");
				for(final Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
					manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
				}
				logger.log(Level.INFO, name + " enabled !");
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
