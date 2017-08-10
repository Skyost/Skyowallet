package fr.skyost.skyowallet;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
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
	 * Skyowallet's config.
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
			final SyncTask syncTask = new SyncTask(config.silentSync ? null : Bukkit.getConsoleSender(), null);
			if(config.mySQLEnable) {
				SkyowalletAPI.getSyncManager().enableMySQL(config.mySQLHost, config.mySQLPort, config.mySQLDB, config.mySQLUser, config.mySQLPassword);
			}
			if(config.autoSyncInterval > 0) {
				Bukkit.getScheduler().scheduleSyncRepeatingTask(this, syncTask, 0L, config.autoSyncInterval * 20L);
			}
			else {
				syncTask.start();
				syncTask.join();
			}
			final SkyowalletCommand skyowalletCmd = new SkyowalletCommand();
			for(final CommandInterface command : new CommandInterface[]{new SkyowalletInfos(), new SkyowalletPay(), new SkyowalletSet(), new SkyowalletSync(), new SkyowalletTop(), new SkyowalletView()}) {
				skyowalletCmd.registerSubCommand(command);
			}
			final PluginCommand skyowallet = getCommand("skyowallet");
			skyowallet.setUsage("/" + skyowallet.getName() + " " + skyowalletCmd.getUsage());
			skyowallet.setExecutor(skyowalletCmd);
			final BankCommand bankCmd = new BankCommand();
			for(final CommandInterface command : new CommandInterface[]{new BankApprove(), new BankCancel(), new BankCreate(), new BankDelete(), new BankDeny(), new BankDeposit(), new BankInfos(), new BankJoin(), new BankLeave(), new BankList(), new BankRemoveOwner(), new BankSetOwner(), new BankToggleApprovalNeeded(), new BankWithdraw()}) {
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
				new MetricsLite(this);
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
			loadExtensions();
			if(manager.getPlugin("Vault") != null) {
				VaultHook.addToVault();
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
			for(final SkyowalletExtension extension : SkyowalletAPI.getLoadedExtensions()) {
				SkyowalletAPI.unregisterExtension(extension, true);
			}
			final SyncTask task = new SyncTask(config.silentSync ? null : Bukkit.getConsoleSender(), null);
			task.start();
			task.join();
			SkyowalletAPI.getSyncManager().closeMySQLConnection();
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Loads Skyowallet's extensions.
	 */
	
	private final void loadExtensions() {
		for(final SkyowalletExtension extension : new SkyowalletExtension[]{new Mine4Cash(this), new CommandsCosts(this), new GoodbyeWallet(this), new ScoreboardInfos(this), new KillerIncome(this), new Bounties(this)}) {
			SkyowalletAPI.registerExtension(extension, true, this);
		}
	}

}