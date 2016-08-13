package fr.skyost.skyowallet;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import fr.skyost.skyowallet.events.*;
import fr.skyost.skyowallet.utils.Utils;

/**
 * SkyowalletAPI.
 * 
 * @author <a href="http://www.skyost.eu"><b>Skyost</b></a>.
 */

public class SkyowalletAPI {
	
	private static final Plugin PLUGIN = Bukkit.getPluginManager().getPlugin("Skyowallet");
	
	private static final String MYSQL_TABLE_ACCOUNTS = "skyowallet_accounts_v3";
	private static final String MYSQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE_ACCOUNTS + " (uuid BINARY(16) NOT NULL, wallet DOUBLE NOT NULL DEFAULT 0.0, bank VARCHAR(30), bank_balance DOUBLE NOT NULL DEFAULT 0.0, is_bank_owner BOOLEAN NOT NULL DEFAULT false, last_modification_time BIGINT NOT NULL, PRIMARY KEY(uuid))";
	private static final String MYSQL_SELECT = "SELECT HEX(uuid) AS uuid, wallet, bank, bank_balance, is_bank_owner, last_modification_time FROM " + MYSQL_TABLE_ACCOUNTS;
	private static final String MYSQL_INSERT_REQUEST = "INSERT INTO " + MYSQL_TABLE_ACCOUNTS + "(`uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `last_modification_time`) VALUES (UNHEX('%s'), %s, '%s', %s, %b, %d) ON DUPLICATE KEY UPDATE `wallet`=VALUES(`wallet`), `bank`=VALUES(`bank`), `bank_balance`=VALUES(`bank_balance`), `is_bank_owner`=VALUES(`is_bank_owner`), `last_modification_time`=(`last_modification_time`)";
	
	protected static Statement statement;
	
	private static final HashMap<UUID, SkyowalletAccount> accounts = new HashMap<UUID, SkyowalletAccount>();
	private static final HashMap<String, SkyowalletBank> banks = new HashMap<String, SkyowalletBank>();
	
	public static final Skyowallet getPlugin() {
		return (Skyowallet)PLUGIN;
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
	
	public static final String getCurrencyName(final double amount) {
		return amount < 2 ? Skyowallet.config.currencyNameSingular : Skyowallet.config.currencyNamePlural;
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
			accountsDir.mkdir();
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
			banksDir.mkdir();
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
			extensionsDir.mkdir();
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
		return accounts.containsKey(uuid);
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
		return accounts.get(uuid);
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
	 * Gets all banks.
	 * 
	 * @return All banks.
	 */
	
	public static final SkyowalletBank[] getBanks() {
		final Collection<SkyowalletBank> banks = SkyowalletAPI.banks.values();
		return banks.toArray(new SkyowalletBank[banks.size()]);
	}
	
	/**
	 * Registers a new account for the specified UUID.
	 * 
	 * @param uuid The UUID.
	 * 
	 * @return The new account.
	 */
	
	public static final SkyowalletAccount registerAccount(final UUID uuid) {
		final SkyowalletAccount account = new SkyowalletAccount(uuid);
		accounts.put(uuid, account);
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
		final SkyowalletBank bank = new SkyowalletBank(name);
		banks.put(name, bank);
		return bank;
	}
	
	/**
	 * Deletes a bank.
	 * 
	 * @param bank The bank.
	 * 
	 * @return An HashMap containing deleted accounts.
	 * <br><b>Key :</b> The account.
	 * <br><b>Value :</b> The account's bank balance.
	 */
	
	public static final HashMap<SkyowalletAccount, Double> deleteBank(final SkyowalletBank bank) {
		final HashMap<SkyowalletAccount, Double> members = bank.getMembers();
		for(final SkyowalletAccount account : members.keySet()) {
			account.setBank(null, false);
		}
		banks.put(bank.getName(), null);
		return members;
	}
	
	/**
	 * Synchronizes the accounts' databases.
	 * <br>NOTE : files are created after the synchronization is done. So, if you manually create files, they will not be loaded.
	 * <br>Thanks to http://stackoverflow.com/a/10951183/3608831.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 */
	
	public static final void sync(final CommandSender sender) {
		final PluginManager manager = Bukkit.getPluginManager();
		final String prefix = sender instanceof Player ? "" : "[Skyowallet] ";
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization started...");
		}
		final SyncBeginEvent syncBeginEvent = new SyncBeginEvent();
		manager.callEvent(syncBeginEvent);
		if(syncBeginEvent.isCancelled()) {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.DARK_RED + "Synchronization cancelled !");
			}
			return;
		}
		if(accounts.size() == 0) {
			try {
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Loading accounts...");
				}
				for(final File localAccount : getAccountsDirectory().listFiles()) {
					if(localAccount.isFile()) {
						final SkyowalletAccount account = SkyowalletAccount.fromJson(Files.readFirstLine(localAccount, Charsets.UTF_8));
						accounts.put(account.getUUID(), account);
					}
				}
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.GREEN + "Accounts loaded.");
				}
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Failed to load accounts.");
				}
			}
			try {
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Loading banks...");
				}
				for(final File localBank : getBanksDirectory().listFiles()) {
					if(localBank.isFile()) {
						final SkyowalletBank bank = SkyowalletBank.fromJSON(Files.readFirstLine(localBank, Charsets.UTF_8));
						banks.put(bank.getName(), bank);
					}
				}
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.GREEN + "Banks loaded.");
				}
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Failed to load banks.");
				}
			}
		}
		if(Skyowallet.config.mySQLEnable) {
			try {
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Synchronization with the MySQL database...");
				}
				if(statement == null || statement.isClosed()) {
					sender.sendMessage(prefix + ChatColor.AQUA + "Logging in to the specified MySQL server...");
					statement = DriverManager.getConnection("jdbc:mysql://" + Skyowallet.config.mySQLHost + ":" + Skyowallet.config.mySQLPort + "/" + Skyowallet.config.mySQLDB, Skyowallet.config.mySQLUser, Skyowallet.config.mySQLPassword).createStatement();
					if(statement == null) {
						statement.executeUpdate(MYSQL_CREATE_TABLE);
					}
					sender.sendMessage(prefix + ChatColor.GREEN + "Done !");
				}
				final HashMap<UUID, SkyowalletAccount> remoteAccounts = new HashMap<UUID, SkyowalletAccount>();
				final ResultSet result = statement.executeQuery(MYSQL_SELECT);
				while(result.next()) {
					final UUID uuid = Utils.uuidTryParse(Utils.uuidAddDashes(result.getString("uuid")));
					if(uuid == null) {
						continue;
					}
					remoteAccounts.put(uuid, new SkyowalletAccount(uuid, result.getDouble("wallet"), result.getString("bank"), result.getDouble("bank_balance"), result.getBoolean("is_bank_owner"), result.getLong("last_modification_time")));
				}
				for(final SkyowalletAccount account : remoteAccounts.values()) {
					final SkyowalletAccount localAccount = accounts.get(account.getUUID());
					if(localAccount == null || localAccount.getLastModificationTime() < account.getLastModificationTime()) {
						accounts.put(account.getUUID(), account);
					}
				}
				for(final SkyowalletAccount account : accounts.values()) {
					final SkyowalletAccount remoteAccount = remoteAccounts.get(account.getUUID());
					final long lastModificationTime = account.getLastModificationTime();
					if(remoteAccount == null || remoteAccount.getLastModificationTime() < lastModificationTime) {
						statement.executeUpdate(String.format(MYSQL_INSERT_REQUEST, account.getUUID().toString().replace("-", ""), String.valueOf(account.getWallet()), account.getBank(), String.valueOf(account.getBankBalance()), account.isBankOwner(), lastModificationTime));
					}
				}
				statement.close();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.GREEN + "Successfully synchronized MySQL database.");
				}
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Error in a MySQL statement !");
				}
			}
		}
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Saving accounts...");
			}
			for(final SkyowalletAccount account : accounts.values()) {
				Files.write(account.toString(), new File(getAccountsDirectoryName(), account.getUUID().toString()), Charsets.UTF_8);
			}
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.GREEN + "Accounts saved with success.");
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to save accounts !");
			}
		}
		try {
			sender.sendMessage(prefix + ChatColor.AQUA + "Saving banks...");
			final List<String> removedBanks = new ArrayList<String>();
			for(final Entry<String, SkyowalletBank> entry : banks.entrySet()) {
				final String bankName = entry.getKey();
				final File bankFile = new File(getBanksDirectoryName(), bankName);
				final SkyowalletBank bank = entry.getValue();
				if(bank == null) {
					removedBanks.add(bankName);
					if(bankFile.exists() && bankFile.isFile()) {
						bankFile.delete();
					}
					continue;
				}
				Files.write(bank.toString(), bankFile, Charsets.UTF_8);
			}
			for(final String removedBank : removedBanks) {
				banks.remove(removedBank);
			}
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.GREEN + "Banks saved with success.");
			}
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to save banks !");
			}
		}
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization finished.");
		}
		manager.callEvent(new SyncEndEvent());
	}
	
}