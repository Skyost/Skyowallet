package fr.skyost.skyowallet;

import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import fr.skyost.skyowallet.command.BankCommand;
import fr.skyost.skyowallet.command.SkyowalletCommand;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.command.subcommands.bank.BankApprove;
import fr.skyost.skyowallet.command.subcommands.bank.BankCancel;
import fr.skyost.skyowallet.command.subcommands.bank.BankCreate;
import fr.skyost.skyowallet.command.subcommands.bank.BankDelete;
import fr.skyost.skyowallet.command.subcommands.bank.BankDeny;
import fr.skyost.skyowallet.command.subcommands.bank.BankDeposit;
import fr.skyost.skyowallet.command.subcommands.bank.BankInfo;
import fr.skyost.skyowallet.command.subcommands.bank.BankJoin;
import fr.skyost.skyowallet.command.subcommands.bank.BankLeave;
import fr.skyost.skyowallet.command.subcommands.bank.BankList;
import fr.skyost.skyowallet.command.subcommands.bank.BankRemoveOwner;
import fr.skyost.skyowallet.command.subcommands.bank.BankSetOwner;
import fr.skyost.skyowallet.command.subcommands.bank.BankToggleApprovalNeeded;
import fr.skyost.skyowallet.command.subcommands.bank.BankWithdraw;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletInfo;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletPay;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletSet;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletSync;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletTop;
import fr.skyost.skyowallet.command.subcommands.skyowallet.SkyowalletView;
import fr.skyost.skyowallet.config.PluginConfig;
import fr.skyost.skyowallet.config.PluginMessages;
import fr.skyost.skyowallet.economy.EconomyOperations;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountFactory;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBankFactory;
import fr.skyost.skyowallet.economy.bank.SkyowalletBankManager;
import fr.skyost.skyowallet.extension.Bounty;
import fr.skyost.skyowallet.extension.CommandCost;
import fr.skyost.skyowallet.extension.ExtensionManager;
import fr.skyost.skyowallet.extension.GoodbyeWallet;
import fr.skyost.skyowallet.extension.KillerIncome;
import fr.skyost.skyowallet.extension.Mine4Cash;
import fr.skyost.skyowallet.extension.ScoreboardInfo;
import fr.skyost.skyowallet.extension.SkyowalletExtension;
import fr.skyost.skyowallet.hook.VaultHook;
import fr.skyost.skyowallet.listener.GlobalEvents;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.sync.SyncTask;
import fr.skyost.skyowallet.sync.queue.FullSyncQueue;
import fr.skyost.skyowallet.util.Skyupdater;

/**
 * The plugin's main class.
 */

public class Skyowallet extends JavaPlugin {
	
	/**
	 * Skyowallet's config.
	 */
	
	private PluginConfig config;
	
	/**
	 * Skyowallet's messages.
	 */
	
	private PluginMessages messages;

	/**
	 * The synchronization manager.
	 */

	private SyncManager syncManager;

	/**
	 * The extension manager.
	 */

	private ExtensionManager extensionManager;

	/**
	 * The account factory.
	 */

	private SkyowalletAccountFactory accountFactory;

	/**
	 * The account manager.
	 */

	private SkyowalletAccountManager accountManager;

	/**
	 * The bank factory.
	 */

	private SkyowalletBankFactory bankFactory;

	/**
	 * The bank manager.
	 */

	private SkyowalletBankManager bankManager;

	/**
	 * Economy operations.
	 */

	private EconomyOperations economyOperations;
	
	@Override
	public final void onEnable() {
		final PluginManager manager = Bukkit.getPluginManager();
		try {
			Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
				Bukkit.getConsoleSender().sendMessage("[" + getName() + "] " + ChatColor.RED + "An uncaught error occurred. Don't hesitate to report it here : https://github.com/Skyost/Skyowallet/issues.");
				throwable.printStackTrace();
			});

			final File dataFolder = getDataFolder();
			config = new PluginConfig(dataFolder);
			config.load();
			messages = new PluginMessages(dataFolder);
			messages.load();

			accountFactory = new SkyowalletAccountFactory(this);
			accountManager = new SkyowalletAccountManager(this);

			bankFactory = new SkyowalletBankFactory(this);
			bankManager = new SkyowalletBankManager();

			economyOperations = new EconomyOperations(this);

			syncManager = new SyncManager(this);

			if(config.mySQLEnable) {
				syncManager.enableMySQL();
			}
			if(config.syncInterval > 0) {
				new SyncTask(this, syncManager.getMainSyncQueue()).runTaskTimer(this, config.syncInterval * 20L, config.syncInterval * 20L);
			}

			SyncTask.runDefaultSync(new FullSyncQueue(syncManager, Bukkit.getConsoleSender()));
			
			final SkyowalletCommand skyowalletCmd = new SkyowalletCommand(this);
			for(final CommandInterface command : new CommandInterface[]{new SkyowalletInfo(), new SkyowalletPay(), new SkyowalletSet(), new SkyowalletSync(), new SkyowalletTop(), new SkyowalletView()}) {
				skyowalletCmd.registerSubCommand(command);
			}
			final PluginCommand skyowallet = getCommand("skyowallet");
			skyowallet.setUsage("/" + skyowallet.getName() + " " + skyowalletCmd.getUsage());
			skyowallet.setExecutor(skyowalletCmd);
			
			final BankCommand bankCmd = new BankCommand(this);
			for(final CommandInterface command : new CommandInterface[]{new BankApprove(), new BankCancel(), new BankCreate(), new BankDelete(), new BankDeny(), new BankDeposit(), new BankInfo(), new BankJoin(), new BankLeave(), new BankList(), new BankRemoveOwner(), new BankSetOwner(), new BankToggleApprovalNeeded(), new BankWithdraw()}) {
				bankCmd.registerSubCommand(command);
			}
			final PluginCommand bank = getCommand("bank");
			bank.setUsage("/" + bank.getName() + " " + bankCmd.getUsage());
			bank.setExecutor(bankCmd);
			manager.registerEvents(new GlobalEvents(this), this);
			
			if(config.enableUpdater) {
				new Skyupdater(this, 82182, getFile(), true, true);
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

			if(manager.getPlugin("Vault") != null) {
				new VaultHook(this).register();
			}

			extensionManager = new ExtensionManager(this, new Mine4Cash(this, this), new CommandCost(this, this), new GoodbyeWallet(this, this), new ScoreboardInfo(this, this), new KillerIncome(this, this), new Bounty(this, this));
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			manager.disablePlugin(this);
		}
	}
	
	@Override
	public final void onDisable() {
		try {
			for(final SkyowalletExtension extension : extensionManager.getLoadedExtensions()) {
				extensionManager.unregister(extension);
			}
			new SyncTask(this, new FullSyncQueue(syncManager, Bukkit.getConsoleSender())).run();
			syncManager.disableMySQL();
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public static Skyowallet getInstance() {
		return (Skyowallet)Bukkit.getPluginManager().getPlugin("Skyowallet");
	}

	/**
	 * Returns the plugin config.
	 *
	 * @return The plugin config.
	 */

	public PluginConfig getPluginConfig() {
		return config;
	}

	/**
	 * Sets the plugin config.
	 *
	 * @param config The plugin config.
	 */

	public final void setPluginConfig(final PluginConfig config) {
		this.config = config;
	}

	/**
	 * Returns the plugin messages.
	 *
	 * @return The plugin messages.
	 */

	public PluginMessages getPluginMessages() {
		return messages;
	}

	/**
	 * Sets the plugin messages.
	 *
	 * @param messages The plugin messages.
	 */

	public void setPluginMessages(final PluginMessages messages) {
		this.messages = messages;
	}

	/**
	 * Returns the synchronization manager.
	 *
	 * @return The synchronization manager.
	 */

	public final SyncManager getSyncManager() {
		return syncManager;
	}

	/**
	 * Sets the synchronization manager.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public final void setSyncManager(final SyncManager syncManager) {
		this.syncManager = syncManager;
	}

	/**
	 * Returns the current account factory.
	 *
	 * @return The current account factory.
	 */

	public final SkyowalletAccountFactory getAccountFactory() {
		return accountFactory;
	}

	/**
	 * Sets the account factory.
	 *
	 * @param accountFactory The account factory.
	 */

	public final void setAccountFactory(final SkyowalletAccountFactory accountFactory) {
		this.accountFactory = accountFactory;
	}

	/**
	 * Returns the current account manager.
	 *
	 * @return The current account manager.
	 */

	public final SkyowalletAccountManager getAccountManager() {
		return accountManager;
	}

	/**
	 * Sets the account manager.
	 *
	 * @param accountManager The account manager.
	 */

	public final void setAccountManager(final SkyowalletAccountManager accountManager) {
		this.accountManager = accountManager;
	}

	/**
	 * Returns the current bank factory.
	 *
	 * @return The current bank factory.
	 */

	public final SkyowalletBankFactory getBankFactory() {
		return bankFactory;
	}

	/**
	 * Sets the bank factory.
	 *
	 * @param bankFactory The bank factory.
	 */

	public final void setBankFactory(final SkyowalletBankFactory bankFactory) {
		this.bankFactory = bankFactory;
	}

	/**
	 * Returns the current bank manager.
	 *
	 * @return The current bank manager.
	 */

	public final SkyowalletBankManager getBankManager() {
		return bankManager;
	}

	/**
	 * Sets the bank manager.
	 *
	 * @param bankManager The bank manager.
	 */

	public final void setBankManager(final SkyowalletBankManager bankManager) {
		this.bankManager = bankManager;
	}

	/**
	 * Returns all available economy operations.
	 *
	 * @return All available economy operations.
	 */

	public final EconomyOperations getEconomyOperations() {
		return economyOperations;
	}

	/**
	 * Sets the available economy operations.
	 *
	 * @param economyOperations Economy operations.
	 */

	public final void setEconomyOperations(final EconomyOperations economyOperations) {
		this.economyOperations = economyOperations;
	}

}