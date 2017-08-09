package fr.skyost.skyowallet;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
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
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.commands.SubCommandsExecutor;
import fr.skyost.skyowallet.extensions.SkyowalletExtension;

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
	 * Checks if the specified player has an account.
	 * 
	 * @param player The player.
	 * 
	 * @return <b>true</b> If the player has an account.
	 * <br><b>false</b> Otherwise.
	 */
	
	public static final boolean hasAccount(final OfflinePlayer player) {
		if(!player.hasPlayedBefore()) {
			return false;
		}
		return hasAccount(player.getUniqueId());
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
		return banks.get(name);
	}
	
	/**
	 * Gets all accounts.
	 * 
	 * @return All accounts.
	 */
	
	public static final SkyowalletAccount[] getAccounts() {
		final Collection<SkyowalletAccount> accounts = SkyowalletAPI.accounts.values();
		return accounts.toArray(new SkyowalletAccount[accounts.size()]);
	}
	
	/**
	 * Gets all non-deleted banks.
	 * 
	 * @return All non-deleted banks.
	 */
	
	public static final SkyowalletBank[] getBanks() {
		final Collection<SkyowalletBank> banks = SkyowalletAPI.banks.values();
		for(final SkyowalletBank bank : new HashSet<SkyowalletBank>(banks)) {
			if(bank != null) {
				continue;
			}
			banks.remove(bank);
		}
		return banks.toArray(new SkyowalletBank[banks.size()]);
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
		accounts.put(account.getUUID().toString(), account);
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
		banks.put(bank.getName(), bank);
		return bank;
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
		final File bankFile = new File(SkyowalletAPI.getBanksDirectory(), bank.getName());
		if(bankFile.exists() && bankFile.isFile()) {
			bankFile.delete();
		}
		banks.remove(bank.getName());
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