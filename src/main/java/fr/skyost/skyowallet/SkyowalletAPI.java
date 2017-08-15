package fr.skyost.skyowallet;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.commands.SubCommandsExecutor;
import fr.skyost.skyowallet.extensions.SkyowalletExtension;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.utils.Utils;

/**
 * Skyowallet API.
 * 
 * @author <a href="https://www.skyost.eu"><b>Skyost</b></a>.
 */

public class SkyowalletAPI {
	
	private static final Skyowallet PLUGIN = (Skyowallet)Bukkit.getPluginManager().getPlugin("Skyowallet");
	private static final SyncManager SYNC_MANAGER = new SyncManager();
	
	protected static final HashMap<String, SkyowalletAccount> accounts = new HashMap<String, SkyowalletAccount>();
	protected static final HashMap<String, SkyowalletBank> banks = new HashMap<String, SkyowalletBank>();
	
	private static final HashSet<SkyowalletExtension> extensions = new HashSet<SkyowalletExtension>();
	
	/**
	 * Gets a Skyowallet instance.
	 * 
	 * @return A Skyowallet instance.
	 */
	
	public static final Skyowallet getPlugin() {
		return PLUGIN;
	}
	
	/**
	 * Gets a SyncManager instance.
	 * 
	 * @return A SyncManager instance.
	 */
	
	public static final SyncManager getSyncManager() {
		return SYNC_MANAGER;
	}
	
	/**
	 * Gets the currency name for numbers inferior than 2.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyNameSingular() {
		return Skyowallet.config.currencyNameSingular;
	}
	
	/**
	 * Gets the currency name for numbers superior than 2.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyNamePlural() {
		return Skyowallet.config.currencyNamePlural;
	}
	
	/**
	 * Gets the currency name for the specified number.
	 * 
	 * @param amount The number.
	 * 
	 * @return The currency name.
	 */
	
	public static final String getCurrencyName(final Double amount) {
		return amount == null || amount < 2d ? Skyowallet.config.currencyNameSingular : Skyowallet.config.currencyNamePlural;
	}
	
	/**
	 * Rounds a <b>double</b> according to the config options.
	 * 
	 * @param amount The <b>double</b>.
	 * 
	 * @return The rounded <b>double</b>.
	 */
	
	public static final Double round(final Double amount) {
		if(amount == null || Skyowallet.config.roundingDigits < 0d) {
			return amount;
		}
		return new BigDecimal(amount).setScale(Skyowallet.config.roundingDigits, RoundingMode.HALF_UP).doubleValue();
	}
	
	/**
	 * Gets the fractional digits that allows to round the currency.
	 * 
	 * @return The fractional digits of the currency.
	 */
	
	public static final int getRoundingDigits() {
		return Skyowallet.config.roundingDigits;
	}
	
	/**
	 * Gets the accounts directory (where the plugin stores the accounts).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 * 
	 * @return The accounts directory.
	 */
	
	public static final File getAccountsDirectory() {
		final File accountsDir = new File(Skyowallet.config.accountsDir);
		if(!accountsDir.exists()) {
			accountsDir.mkdirs();
		}
		return accountsDir;
	}
	
	/**
	 * Gets the banks directory (where the plugin stores the banks).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 * 
	 * @return The banks directory.
	 */
	
	public static final File getBanksDirectory() {
		final File banksDir = new File(Skyowallet.config.banksDir);
		if(!banksDir.exists()) {
			banksDir.mkdirs();
		}
		return banksDir;
	}
	
	/**
	 * Gets the extensions directory (where the plugin stores its extensions' config).
	 * <br><b>NOTE :</b> if the directory does not exist, this method will try to create it.
	 * 
	 * @return The extensions directory.
	 */
	
	public static final File getExtensionsDirectory() {
		final File extensionsDir = new File(Skyowallet.config.extensionsDir);
		if(!extensionsDir.exists()) {
			extensionsDir.mkdirs();
		}
		return extensionsDir;
	}
	
	/**
	 * Gets the accounts directory's name. Used in loops.
	 * 
	 * @return The accounts directory's name.
	 */
	
	public static final String getAccountsDirectoryName() {
		return Skyowallet.config.accountsDir;
	}
	
	/**
	 * Gets the banks directory's name. Used in loops.
	 * 
	 * @return The banks directory's name.
	 */
	
	public static final String getBanksDirectoryName() {
		return Skyowallet.config.banksDir;
	}
	
	/**
	 * Gets the global tax rate as specified in the configuration.
	 * 
	 * @return The global tax rate as specified in the configuration.
	 */
	
	public static final double getGlobalTaxRate() {
		return Skyowallet.config.taxesRateGlobal;
	}
	
	/**
	 * Gets the /skyowallet pay tax rate as specified in the configuration.
	 * 
	 * @return The /skyowallet pay tax rate as specified in the configuration.
	 */
	
	public static final double getSkyowalletPayTaxRate() {
		return Skyowallet.config.taxesRateSkyowalletPay;
	}
	
	/**
	 * Gets the /bank deposit tax rate as specified in the configuration.
	 * 
	 * @return The /bank deposit tax rate as specified in the configuration.
	 */
	
	public static final double getBankDepositTaxRate() {
		return Skyowallet.config.taxesRateBankDeposit;
	}
	
	/**
	 * Gets the /bank withdraw tax rate as specified in the configuration.
	 * 
	 * @return The /bank withdraw tax rate as specified in the configuration.
	 */
	
	public static final double getBankWithdrawTaxRate() {
		return Skyowallet.config.taxesRateBankWithdraw;
	}
	
	/**
	 * Taxes a specified amount of money and allocate the taxed money to the accounts specified in the configuration.
	 * 
	 * @param amount Amount of money to tax.
	 * @param taxRate The tax rate.
	 * 
	 * @return The remaining money, once taxed.
	 */
	
	public static final double tax(final double amount, final double taxRate) {
		final ConsoleCommandSender console = Bukkit.getConsoleSender();
		final double taxedAmount = (taxRate * amount) / 100d;
		for(final Entry<String, String> entry : Skyowallet.config.taxesAccounts.entrySet()) {
			final UUID uuid = Utils.uuidTryParse(entry.getKey());
			
			if(uuid == null) {
				console.sendMessage("[" + PLUGIN.getName() + "] " + ChatColor.RED + "Unable to give tax to \"" + uuid + "\" because it is not a valid UUID.");
				continue;
			}
			final Double accountRate = Utils.doubleTryParse(entry.getValue());
			if(accountRate == null) {
				console.sendMessage("[" + PLUGIN.getName() + "] " + ChatColor.RED + "Unable to give tax to \"" + uuid + "\" because the specified rate " + accountRate + " is invalid.");
				continue;
			}
			
			SkyowalletAccount account = getAccount(uuid);
			if(account == null) {
				account = registerAccount(uuid);
			}
			
			final double newAmount = ((accountRate * taxedAmount) / 100);
			if(newAmount == 0d) {
				continue;
			}
			
			if(Skyowallet.config.taxesToBank && account.getBank() != null) {
				account.setBankBalance(account.getBankBalance() + newAmount, 0d, false);
			}
			else {
				account.setWallet(account.getWallet() + newAmount, 0d, false);
			}
			
			if(!Skyowallet.config.taxesNotify) {
				continue;
			}
			final OfflinePlayer player = Bukkit.getOfflinePlayer(account.getUUID());
			if(player != null && player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message48, new AmountPlaceholder(newAmount), new CurrencyNamePlaceholder(newAmount)));
			}
		}
		return amount - taxedAmount;
	}
	
	/**
	 * Checks if the specified player has an account.
	 * 
	 * @param player The player.
	 * 
	 * @return <b>true</b> If the player has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final OfflinePlayer player) {
		return hasAccount(player == null ? null : player.getUniqueId());
	}
	
	/**
	 * Checks if the specified UUID has an account.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return <b>true</b> If the UUID has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final UUID uuid) {
		if(uuid == null) {
			return false;
		}
		return accounts.containsKey(uuid.toString());
	}
	
	/**
	 * Checks if the specified bank exists.
	 * 
	 * @param name The bank's name.
	 * 
	 * @return <b>true</b> If the bank exists.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean isBankExists(final String name) {
		if(name == null || name.length() == 0) {
			return false;
		}
		return banks.containsKey(name);
	}
	
	/**
	 * Gets the player's account.
	 * 
	 * @param player The player.
	 * 
	 * @return The account of the specified player if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final OfflinePlayer player) {
		if(!hasAccount(player)) {
			return null;
		}
		return getAccount(player.getUniqueId());
	}
	
	/**
	 * Gets an account by its UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The account if found (null otherwise).
	 */
	
	public static final SkyowalletAccount getAccount(final UUID uuid) {
		if(uuid == null) {
			return null;
		}
		return accounts.get(uuid.toString());
	}
	
	/**
	 * Gets a bank by its name.
	 * 
	 * @param name The name.
	 * 
	 * @return The bank if found (null otherwise).
	 */
	
	public static final SkyowalletBank getBank(final String name) {
		if(name == null || name.length() == 0) {
			return null;
		}
		return banks.get(name);
	}
	
	/**
	 * Gets all accounts.
	 * 
	 * @return All accounts.
	 */
	
	public static final SkyowalletAccount[] getAccounts() {
		return accounts.values().toArray(new SkyowalletAccount[accounts.size()]);
	}
	
	/**
	 * Gets all non-deleted banks.
	 * 
	 * @return All non-deleted banks.
	 */
	
	public static final SkyowalletBank[] getBanks() {
		return banks.values().toArray(new SkyowalletBank[banks.size()]);
	}
	
	/**
	 * Registers the specified object by its identifier.
	 * 
	 * @param object The object.
	 */
	
	protected static final void register(final SkyowalletObject object) {
		if(object instanceof SkyowalletAccount) {
			registerAccount((SkyowalletAccount)object);
			return;
		}
		createBank((SkyowalletBank)object);
	}
	
	/**
	 * Registers a new account for the specified UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The new account.
	 */
	
	public static final SkyowalletAccount registerAccount(final UUID uuid) {
		if(uuid == null) {
			return null;
		}
		return registerAccount(new SkyowalletAccount(uuid));
	}
	
	/**
	 * Registers a new account.
	 * 
	 * @param account The account.
	 * 
	 * @return The account.
	 */
	
	protected static final SkyowalletAccount registerAccount(final SkyowalletAccount account) {
		accounts.put(account.getIdentifier(), account);
		return account;
	}
	
	/**
	 * Creates a new bank with the specified name.
	 * 
	 * @param name The name.
	 * 
	 * @return The new bank.
	 */
	
	public static final SkyowalletBank createBank(final String name) {
		if(name == null || name.length() == 0) {
			return null;
		}
		return createBank(new SkyowalletBank(name));
	}
	
	/**
	 * Creates a new bank.
	 * 
	 * @param bank The bank.
	 * 
	 * @return The bank.
	 */
	
	protected static final SkyowalletBank createBank(final SkyowalletBank bank) {
		banks.put(bank.getIdentifier(), bank);
		return bank;
	}
	
	/**
	 * Deletes an account.
	 * 
	 * @param uuid The account uuid.
	 * 
	 * @return An array containing its wallet and its bank balance.
	 */
	
	public static final double[] deleteAccount(final UUID uuid) {
		if(uuid == null) {
			return new double[]{-1d, -1d};
		}
		return deleteAccount(SkyowalletAPI.getAccount(uuid));
	}
	
	/**
	 * Deletes an account.
	 * 
	 * @param account The account.
	 * 
	 * @return An array containing its wallet and its bank balance.
	 */
	
	public static final double[] deleteAccount(final SkyowalletAccount account) {
		if(account == null) {
			return new double[]{-1d, -1d};
		}
		final String identifier = account.getIdentifier();
		final File accountFile = new File(SkyowalletAPI.getAccountsDirectory(), identifier);
		if(accountFile.exists() && accountFile.isFile()) {
			accountFile.delete();
		}
		accounts.remove(identifier);
		return new double[]{account.getWallet(), account.getBankBalance()};
	}
	
	/**
	 * Deletes a bank.
	 * 
	 * @param bank The bank's name.
	 * 
	 * @return An HashMap containing deleted accounts.
	 * <br><b>Key :</b> The account.
	 * <br><b>Value :</b> The account's bank balance (-1d if the account was just asking to join the bank).
	 */
	
	public static final HashMap<SkyowalletAccount, Double> deleteBank(final String bank) {
		if(bank == null) {
			return null;
		}
		return deleteBank(SkyowalletAPI.getBank(bank));
	}
	
	/**
	 * Deletes a bank.
	 * 
	 * @param bank The bank.
	 * 
	 * @return An HashMap containing deleted accounts.
	 * <br><b>Key :</b> The account.
	 * <br><b>Value :</b> The account's bank balance (-1d if the account was just asking to join the bank).
	 */
	
	public static final HashMap<SkyowalletAccount, Double> deleteBank(final SkyowalletBank bank) {
		final HashMap<SkyowalletAccount, Double> members = bank.getMembers();
		for(final SkyowalletAccount account : members.keySet()) {
			account.setBank(null, false);
		}
		for(final SkyowalletAccount account : bank.getPendingMembers()) {
			account.setBankRequest(null, false);
			members.put(account, -1d);
		}
		final String identifier = bank.getIdentifier();
		final File bankFile = new File(SkyowalletAPI.getBanksDirectory(), identifier);
		if(bankFile.exists() && bankFile.isFile()) {
			bankFile.delete();
		}
		banks.remove(identifier);
		return members;
	}
	
	/**
	 * Convenience method for {@link fr.skyost.skyowallet.SyncManager#runFullSync(CommandSender)}.
	 */
	
	public static final void sync(final CommandSender sender) {
		SYNC_MANAGER.runFullSync(sender);
	}
	
	/**
	 * Convenience method for {@link fr.skyost.skyowallet.SyncManager#runSync(CommandSender, SkyowalletAccount)}.
	 */
	
	public static final void sync(final CommandSender sender, final SkyowalletAccount account) {
		SYNC_MANAGER.runSync(sender, account);
	}
	
	/**
	 * Loads and registers an extension.
	 * 
	 * @param extension The Skyowallet extension.
	 * @param log If there should be a log in the console.
	 * @param plugin The plugin this extension belongs to.
	 */
	
	public static final void registerExtension(final SkyowalletExtension extension, final boolean log, final JavaPlugin plugin) {
		final Logger logger = plugin.getLogger();
		final String name = extension.getName();
		try {
			extension.load();
			if(!extension.isEnabled()) {
				extension.unload();
				return;
			}
			if(log) {
				logger.log(Level.INFO, "Loading " + name + "...");
			}
			final PluginManager manager = Bukkit.getPluginManager();
			for(final Entry<String, PermissionDefault> entry : extension.getPermissions().entrySet()) {
				manager.addPermission(new Permission(entry.getKey(), entry.getValue()));
			}
			for(final Entry<String, CommandExecutor> entry : extension.getCommands().entrySet()) {
				final CommandExecutor executor = entry.getValue();
				final PluginCommand command = plugin.getCommand(entry.getKey());
				command.setUsage(ChatColor.RED + "/" + command.getName() + " " + (executor instanceof SubCommandsExecutor ? ((SubCommandsExecutor)executor).getUsage() : command.getUsage()));
				command.setExecutor(executor);
			}
			extensions.add(extension);
			if(log) {
				logger.log(Level.INFO, name + " loaded !");
			}
		}
		catch(final Exception ex) {
			if(log) {
				logger.log(Level.SEVERE, "An error occured while verifying / enabling the extension \"" + name + "\" : " + ex.getClass().getName() + ".");
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Unloads and unregisters an extension (just a convenience method).
	 * 
	 * @param extension The Skyowallet extension.
	 * @param log If there should be a log in the console.
	 * 
	 * @throws InvalidConfigurationException If the config cannot be saved.
	 */
	
	public static final void unregisterExtension(final SkyowalletExtension extension, final boolean log) throws InvalidConfigurationException {
		final Logger logger = extension.getPlugin().getLogger();
		final String name = extension.getName();
		if(log) {
			logger.log(Level.INFO, "Disabling " + name + "...");
		}
		extension.unload();
		extensions.remove(extension);
		if(log) {
			logger.log(Level.INFO, name + " disabled !");
		}
	}
	
	/**
	 * Gets the loaded and registered extensions.
	 * 
	 * @return The loaded and registered extensions.
	 */
	
	public static final Set<SkyowalletExtension> getLoadedExtensions() {
		return new HashSet<SkyowalletExtension>(extensions);
	}
	
}