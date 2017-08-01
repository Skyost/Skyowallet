package fr.skyost.skyowallet;

import java.io.File;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import fr.skyost.skyowallet.events.SyncBeginEvent;
import fr.skyost.skyowallet.events.SyncEndEvent;
import fr.skyost.skyowallet.utils.Utils;

/**
 * This helper class allows to sync data of this plugin.
 * 
 * @author <a href="https://www.skyost.eu"><b>Skyost</b></a>.
 */

public class SyncManager {
	
	/**
	 * The MySQL table that contains accounts.
	 */
	
	public static final String MYSQL_TABLE_ACCOUNTS = "skyowallet_accounts_v4";
	
	/**
	 * The MySQL query that allows to create a table.
	 */
	
	private static final String MYSQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE_ACCOUNTS + " (uuid BINARY(16) NOT NULL, wallet DOUBLE NOT NULL DEFAULT 0.0, bank VARCHAR(30), bank_balance DOUBLE NOT NULL DEFAULT 0.0, is_bank_owner BOOLEAN NOT NULL DEFAULT false, last_modification_time BIGINT NOT NULL, PRIMARY KEY(uuid))";
	
	/**
	 * The MySQL query that selects necessary data from the table.
	 */
	
	private static final String MYSQL_SELECT = "SELECT HEX(uuid) AS uuid, wallet, bank, bank_balance, is_bank_owner, last_modification_time FROM " + MYSQL_TABLE_ACCOUNTS;
	
	/**
	 * The MySQL query that inserts data to the table.
	 */
	
	private static final String MYSQL_INSERT_REQUEST = "INSERT INTO " + MYSQL_TABLE_ACCOUNTS + "(`uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `last_modification_time`) VALUES (UNHEX('%s'), %s, %s, %s, %b, %d) ON DUPLICATE KEY UPDATE `wallet`=VALUES(`wallet`), `bank`=VALUES(`bank`), `bank_balance`=VALUES(`bank_balance`), `is_bank_owner`=VALUES(`is_bank_owner`), `last_modification_time`=(`last_modification_time`)";
	
	/**
	 * The UUID field.
	 */
	
	public static final String MYSQL_FIELD_UUID = "uuid";
	
	/**
	 * The wallet field.
	 */
	
	public static final String MYSQL_FIELD_WALLET = "wallet";
	
	/**
	 * The bank field.
	 */
	
	public static final String MYSQL_FIELD_BANK = "bank";
	
	/**
	 * The bank balance field.
	 */
	
	public static final String MYSQL_FIELD_BANK_BALANCE = "bank_balance";
	
	/**
	 * The is owner field.
	 */
	
	public static final String MYSQL_FIELD_IS_BANK_OWNER = "is_bank_owner";
	
	/**
	 * The last modification time field.
	 */
	
	public static final String MYSQL_FIELD_LAST_MODIFICATION_TIME = "last_modification_time";
	
	/**
	 * Whether MySQL is enabled.
	 */
	
	private boolean mySQL;
	
	/**
	 * The MySQL host.
	 */
	
	private String host;
	
	/**
	 * The MySQL port.
	 */
	
	private int port;
	
	/**
	 * The MySQL database.
	 */
	
	private String database;
	
	/**
	 * The MySQL username.
	 */
	
	private String username;
	
	/**
	 * The MySQL password.
	 */
	
	private String password;
	
	/**
	 * The current MySQL statement.
	 */
	
	private Statement statement;
	
	/**
	 * Enables MySQL.
	 * 
	 * @param host The MySQL host.
	 * @param port The MySQL port.
	 * @param database The MySQL database.
	 * @param username The MySQL username.
	 * @param password The MySQL password.
	 */
	
	public void enableMySQL(final String host, final int port, final String database, final String username, final String password) {
		this.mySQL = true;
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Disables MySQL.
	 * 
	 * @throws SQLException If an exception occurs while closing the MySQL connection.
	 */
	
	public final void disableMySQL() throws SQLException {
		closeMySQLConnection();
		mySQL = false;
		host = null;
		port = 0;
		database = null;
		username = null;
		password = null;
	}
	
	/**
	 * Executes an update via the current statement (which must be opened).
	 * 
	 * @param request The request that will be formatted with <b>String.format(...)</b>.
	 * @param parameters The parameters.
	 * 
	 * @return The result of <b>statement.executeUpdate(...)</b>.
	 * 
	 * @throws SQLException If an exception occurs while executing the update.
	 */
	
	public final int executeUpdate(String request, final Object... parameters) throws SQLException {
		request = String.format(request, (Object[])parameters);
		return statement.executeUpdate(request);
	}
	
	/**
	 * Executes a query via the current statement (which must be opened).
	 * 
	 * @param request The request that will be formatted with <b>String.format(...)</b>.
	 * @param parameters The parameters.
	 * 
	 * @return The result of <b>statement.executeQuery(...)</b>.
	 * 
	 * @throws SQLException If an exception occurs while executing the query.
	 */
	
	public final ResultSet executeQuery(String request, final Object... parameters) throws SQLException {
		request = String.format(request, (Object[])parameters);
		return statement.executeQuery(request);
	}
	
	/**
	 * Opens a MySQL connection.
	 * 
	 * @throws SQLException If an exception occurs while opening the connection.
	 */
	
	public final void openMySQLConnection() throws SQLException {
		if(!mySQL || !isMySQLConnectionClosed()) {
			return;
		}
		statement = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password).createStatement();
	}
	
	/**
	 * Checks if a MySQL connection is currently opened.
	 * 
	 * @return Whether a MySQL connection is currently opened.
	 * 
	 * @throws SQLException If an exception occurs while executing <b>statement.isClosed()</b>.
	 */
	
	public final boolean isMySQLConnectionClosed() throws SQLException {
		return statement == null ? true : statement.isClosed();
	}
	
	/**
	 * Closes the current MySQL connection.
	 * 
	 * @throws SQLException If an exception occurs while closing the connection.
	 */
	
	public final void closeMySQLConnection() throws SQLException {
		if(!mySQL || isMySQLConnectionClosed()) {
			return;
		}
		statement.getConnection().close();
		statement.close();
		statement = null;
	}
	
	/**
	 * Synchronizes the accounts' databases.
	 * <br>Thanks to http://stackoverflow.com/a/10951183/3608831.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 */
	
	public final void sync(final CommandSender sender) {
		final PluginManager manager = Bukkit.getPluginManager();
		final String prefix = sender instanceof Player ? "" : "[" + SkyowalletAPI.getPlugin().getName() + "] ";
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
		loadAccounts(sender, prefix);
		loadBanks(sender, prefix);
		if(mySQL) {
			mySQLSync(sender, prefix);
		}
		saveAccounts(sender, prefix);
		saveBanks(sender, prefix);
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization finished.");
		}
		manager.callEvent(new SyncEndEvent());
	}
	
	/**
	 * Loads accounts.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 */
	
	public final void loadAccounts(final CommandSender sender, final String prefix) {
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Loading accounts...");
			}
			for(final File file : SkyowalletAPI.getAccountsDirectory().listFiles()) {
				if(file.isFile()) {
					final SkyowalletAccount account = SkyowalletAccount.fromJson(Files.readFirstLine(file, Charsets.UTF_8));
					final SkyowalletAccount localAccount = SkyowalletAPI.getAccount(account.getUUID());
					if(localAccount == null) {
						SkyowalletAPI.registerAccount(account);
						continue;
					}
					if(localAccount.getLastModificationTime() > account.getLastModificationTime()) {
						continue;
					}
					SkyowalletAPI.registerAccount(account);
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
	}
	
	/**
	 * Loads banks.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 */
	
	public final void loadBanks(final CommandSender sender, final String prefix) {
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Loading banks...");
			}
			for(final File file : SkyowalletAPI.getBanksDirectory().listFiles()) {
				if(file.isFile()) {
					final SkyowalletBank bank = SkyowalletBank.fromJSON(Files.readFirstLine(file, Charsets.UTF_8));
					SkyowalletAPI.createBank(bank);
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
	
	/**
	 * Runs a MySQL sync.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 */
	
	public final void mySQLSync(final CommandSender sender, final String prefix) {
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Synchronization with the MySQL database...");
				sender.sendMessage(prefix + ChatColor.AQUA + "Logging in to the specified MySQL server...");
			}
			openMySQLConnection();
			executeUpdate(MYSQL_CREATE_TABLE);
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.GREEN + "Done !");
			}
			final HashMap<UUID, SkyowalletAccount> remoteAccounts = new HashMap<UUID, SkyowalletAccount>();
			final ResultSet result = executeQuery(MYSQL_SELECT);
			while(result.next()) {
				final UUID uuid = Utils.uuidTryParse(Utils.uuidAddDashes(result.getString(MYSQL_FIELD_UUID)));
				if(uuid == null) {
					continue;
				}
				final SkyowalletAccount remoteAccount = new SkyowalletAccount(uuid, result.getDouble(MYSQL_FIELD_WALLET), result.getString(MYSQL_FIELD_BANK), result.getDouble(MYSQL_FIELD_BANK_BALANCE), result.getBoolean(MYSQL_FIELD_IS_BANK_OWNER), result.getLong(MYSQL_FIELD_LAST_MODIFICATION_TIME));
				remoteAccounts.put(uuid, remoteAccount);
			}
			for(final SkyowalletAccount account : remoteAccounts.values()) {
				final SkyowalletAccount localAccount = SkyowalletAPI.getAccount(account.getUUID());
				if(localAccount == null || localAccount.getLastModificationTime() < account.getLastModificationTime()) {
					SkyowalletAPI.registerAccount(account);
				}
			}
			for(final SkyowalletAccount account : SkyowalletAPI.getAccounts()) {
				final SkyowalletAccount remoteAccount = remoteAccounts.get(account.getUUID());
				final long lastModificationTime = account.getLastModificationTime();
				if(remoteAccount == null || remoteAccount.getLastModificationTime() < lastModificationTime) {
					final SkyowalletBank bank = account.getBank();
					executeUpdate(MYSQL_INSERT_REQUEST, account.getUUID().toString().replace("-", ""), String.valueOf(account.getWallet()), bank == null ? "NULL" : "'" + bank.getName() + "'", String.valueOf(account.getBankBalance()), account.isBankOwner(), lastModificationTime);
				}
			}
			closeMySQLConnection();
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
	
	/**
	 * Saves accounts.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 */
	
	public final void saveAccounts(final CommandSender sender, final String prefix) {
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Saving accounts...");
			}
			final File accountsDirectory = SkyowalletAPI.getAccountsDirectory();
			if(!accountsDirectory.exists()) {
				accountsDirectory.mkdirs();
			}
			for(final SkyowalletAccount account : SkyowalletAPI.getAccounts()) {
				if(account == null) {
					continue;
				}
				Files.write(account.toString(), new File(accountsDirectory, account.getUUID().toString()), Charsets.UTF_8);
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
	}
	
	/**
	 * Saves banks.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 */
	
	public final void saveBanks(final CommandSender sender, final String prefix) {
		try {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.AQUA + "Saving banks...");
			}
			final File banksDirectory = SkyowalletAPI.getBanksDirectory();
			if(!banksDirectory.exists()) {
				banksDirectory.mkdirs();
			}
			final HashSet<String> deletedBanks = new HashSet<String>();
			for(final Entry<String, SkyowalletBank> entry : SkyowalletAPI.banks.entrySet()) {
				final String name = entry.getKey();
				final SkyowalletBank bank = entry.getValue();
				final File file = new File(banksDirectory, name);
				if(entry.getValue() == null) {
					deletedBanks.add(name);
					if(file.exists() && file.isFile()) {
						file.delete();
					}
					continue;
				}
				Files.write(bank.toString(), file, Charsets.UTF_8);
			}
			for(final String deletedBank : deletedBanks) {
				SkyowalletAPI.banks.remove(deletedBank);
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
	}
	
}