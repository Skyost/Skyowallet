package fr.skyost.skyowallet;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.json.simple.parser.ParseException;

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
	
	public static final String MYSQL_TABLE_ACCOUNTS = "skyowallet_accounts_v5";
	
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
	 * The bank pending approval field.
	 */
	
	public static final String MYSQL_FIELD_BANK_REQUEST = "bank_request";
	
	/**
	 * The last modification time field.
	 */
	
	public static final String MYSQL_FIELD_LAST_MODIFICATION_TIME = "last_modification_time";
	
	/**
	 * The MySQL query that allows to create a table.
	 */
	
	private static final String MYSQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE_ACCOUNTS + " (" + MYSQL_FIELD_UUID + " BINARY(16) NOT NULL COMMENT 'UUID of the player, inserted with UNHEX(...) and with dashes removed.', " + MYSQL_FIELD_WALLET + " DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Wallet of the player.', " + MYSQL_FIELD_BANK + " VARCHAR(30) COMMENT 'Bank name of the player (or NULL if no bank).', " + MYSQL_FIELD_BANK_BALANCE + " DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Bank balance of the player.', " + MYSQL_FIELD_IS_BANK_OWNER + " BOOLEAN NOT NULL DEFAULT false COMMENT '0 if the player is an owner of its bank, 1 otherwise. The bank field must not be NULL if you want to change this field.', " + MYSQL_FIELD_BANK_REQUEST + " VARCHAR(30) COMMENT 'Name of the bank this player requested to join. The bank must be NULL if you want to change this field.', " + MYSQL_FIELD_LAST_MODIFICATION_TIME + " BIGINT NOT NULL COMMENT 'The ellapsed time since January 1st 1970 in milliseconds. MUST BE UPDATED AFTER EACH CHANGE !', PRIMARY KEY(" + MYSQL_FIELD_UUID + "))";
	
	/**
	 * The MySQL query that selects necessary data from the table.
	 */
	
	private static final String MYSQL_SELECT = "SELECT HEX(" + MYSQL_FIELD_UUID + ") AS " + MYSQL_FIELD_UUID + ", " + MYSQL_FIELD_WALLET + ", " + MYSQL_FIELD_BANK + ", " + MYSQL_FIELD_BANK_BALANCE + ", " + MYSQL_FIELD_IS_BANK_OWNER + ", " + MYSQL_FIELD_BANK_REQUEST + ", " + MYSQL_FIELD_LAST_MODIFICATION_TIME + " FROM " + MYSQL_TABLE_ACCOUNTS;
	
	/**
	 * The MySQL query that inserts data to the table.
	 */
	
	private static final String MYSQL_INSERT_REQUEST = "INSERT INTO " + MYSQL_TABLE_ACCOUNTS + "(`" + MYSQL_FIELD_UUID + "`, `" + MYSQL_FIELD_WALLET + "`, `" + MYSQL_FIELD_BANK + "`, `" + MYSQL_FIELD_BANK_BALANCE + "`, `" + MYSQL_FIELD_IS_BANK_OWNER + "`, `" + MYSQL_FIELD_BANK_REQUEST + "`, `" + MYSQL_FIELD_LAST_MODIFICATION_TIME + "`) VALUES (UNHEX('%s'), %s, %s, %s, %b, %s, %d) ON DUPLICATE KEY UPDATE `" + MYSQL_FIELD_WALLET + "`=VALUES(`" + MYSQL_FIELD_WALLET + "`), `" + MYSQL_FIELD_BANK + "`=VALUES(`" + MYSQL_FIELD_BANK + "`), `" + MYSQL_FIELD_BANK_BALANCE + "`=VALUES(`" + MYSQL_FIELD_BANK_BALANCE + "`), `" + MYSQL_FIELD_IS_BANK_OWNER + "`=VALUES(`" + MYSQL_FIELD_IS_BANK_OWNER + "`), `" + MYSQL_FIELD_BANK_REQUEST + "`=VALUES(`" + MYSQL_FIELD_BANK_REQUEST + "`), `" + MYSQL_FIELD_LAST_MODIFICATION_TIME + "`=VALUES(`" + MYSQL_FIELD_LAST_MODIFICATION_TIME + "`)";
	
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
	
	public final int executeUpdate(final String request, final Object... parameters) throws SQLException {
		return statement.executeUpdate(String.format(request, (Object[])parameters));
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
	
	public final ResultSet executeQuery(final String request, final Object... parameters) throws SQLException {
		return statement.executeQuery(String.format(request, (Object[])parameters));
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
	 * Runs a full synchronization :
	 * <ol>
	 * <li>Loads all accounts located in the accounts folder and compare them with those located in the memory.</li>
	 * <li>Same with banks.</li>
	 * <li>If MySQL is enabled, then runs a MySQL sync (only for accounts).</li>
	 * <li>Saves all accounts in their file.</li>
	 * <li>Same with banks.</li>
	 * </ol>
	 * 
	 * @param sender sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 */
	
	public final void runFullSync(final CommandSender sender) {
		final PluginManager manager = Bukkit.getPluginManager();
		
		final String prefix = sender instanceof Player ? "" : "[" + SkyowalletAPI.getPlugin().getName() + "] ";
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Full synchronization started...");
		}
		final SyncBeginEvent syncBeginEvent = new SyncBeginEvent();
		manager.callEvent(syncBeginEvent);
		if(syncBeginEvent.isCancelled()) {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.DARK_RED + "Synchronization cancelled !");
			}
			return;
		}
		
		// LOADS ACCOUNTS
		try {
			loadObjects(true);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load accounts.");
			}
		}
		
		// LOADS BANKS
		try {
			loadObjects(false);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load banks.");
			}
		}
		
		// MYSQL SYNC
		if(mySQL) {
			try {
				mySQLSync(null);
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Error in a MySQL statement !");
				}
			}
		}
		
		// SAVES ACCOUNTS
		try {
			saveObjects(SkyowalletAPI.getAccountsDirectory(), SkyowalletAPI.getAccounts());
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to save accounts !");
			}
		}
		
		// SAVES BANKS
		try {
			saveObjects(SkyowalletAPI.getBanksDirectory(), SkyowalletAPI.getBanks());
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
	
	/**
	 * Runs a partial synchronization :
	 * <ol>
	 * <li>Loads the account located in the accounts folder and compare it with the one located in the memory.</li>
	 * <li>Same with its bank.</li>
	 * <li>If MySQL is enabled, then runs a MySQL sync (only for the account).</li>
	 * <li>Saves the account in its file.</li>
	 * <li>Same with its bank.</li>
	 * </ol>
	 * 
	 * @param sender sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 */
	
	public final void runSync(final CommandSender sender, final SkyowalletAccount account) {
		final PluginManager manager = Bukkit.getPluginManager();
		
		final String prefix = sender instanceof Player ? "" : "[" + SkyowalletAPI.getPlugin().getName() + "] ";
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Synchronizing the account of " + account.getUUID() + "...");
		}
		final SyncBeginEvent syncBeginEvent = new SyncBeginEvent();
		manager.callEvent(syncBeginEvent);
		if(syncBeginEvent.isCancelled()) {
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.DARK_RED + "Synchronization cancelled !");
			}
			return;
		}
		
		// LOADS ACCOUNT
		try {
			loadObject(true, new File(SkyowalletAPI.getAccountsDirectory(), account.getIdentifier()));
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to load account.");
			}
		}
		
		// LOADS BANK
		final SkyowalletBank bank = account.getBank();
		if(bank != null) {
			try {
				loadObject(false,  new File(SkyowalletAPI.getBanksDirectory(), bank.getIdentifier()));
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Failed to load bank.");
				}
			}
		}
		
		// MYSQL SYNC
		if(mySQL) {
			try {
				mySQLSync(account.getUUID());
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Error in a MySQL statement !");
				}
			}
		}
		
		// SAVES ACCOUNT
		try {
			saveObject(SkyowalletAPI.getAccountsDirectory(), account);
		}
		catch(final Exception ex) {
			ex.printStackTrace();
			if(sender != null) {
				sender.sendMessage(prefix + ChatColor.RED + "Failed to save account !");
			}
		}
		
		// SAVES BANK
		if(bank != null) {
			try {
				saveObject(SkyowalletAPI.getBanksDirectory(), bank);
			}
			catch(final Exception ex) {
				ex.printStackTrace();
				if(sender != null) {
					sender.sendMessage(prefix + ChatColor.RED + "Failed to save bank !");
				}
			}
		}
		
		if(sender != null) {
			sender.sendMessage(prefix + ChatColor.GOLD + "Synchronization finished.");
		}
		manager.callEvent(new SyncEndEvent());
	}
	
	/**
	 * Loads all objects.
	 * 
	 * @param accounts Whether these objects are accounts or banks.
	 * 
	 * @throws ParseException If an exception occurs while parsing JSON.
	 * @throws IllegalAccessException  If an exception occurs while accessing fields.
	 * @throws IllegalArgumentException If an exception occurs while reading JSON.
	 * @throws IOException If an exception occurs while trying to read a file.
	 */
	
	public final void loadObjects(final boolean accounts) throws IllegalArgumentException, IllegalAccessException, ParseException, IOException {
		for(final File file : (accounts ? SkyowalletAPI.getAccountsDirectory() : SkyowalletAPI.getBanksDirectory()).listFiles()) {
			loadObject(accounts, file);
		}
	}
	
	/**
	 * Loads an object.
	 * 
	 * @param accounts Whether this object is an account or a banks.
	 * @param file The file.
	 * 
	 * @throws ParseException If an exception occurs while parsing JSON.
	 * @throws IllegalAccessException  If an exception occurs while accessing fields.
	 * @throws IllegalArgumentException If an exception occurs while reading JSON.
	 * @throws IOException If an exception occurs while trying to read the file.
	 */
	
	public final void loadObject(final boolean accounts, final File file) throws IllegalArgumentException, IllegalAccessException, ParseException, IOException {
		final HashMap<String, ?> currentMap = accounts ? SkyowalletAPI.accounts : SkyowalletAPI.banks;
		if(!file.isFile()) {
			return;
		}
		final SkyowalletObject object = accounts ? SkyowalletAccount.fromJSON(Files.readFirstLine(file, Charsets.UTF_8)) : SkyowalletBank.fromJSON(Files.readFirstLine(file, Charsets.UTF_8));
		final SkyowalletObject localObject = (SkyowalletObject)currentMap.get(object.getIdentifier());
		if(!currentMap.containsKey(file.getName())) {
			SkyowalletAPI.register(object);
			return;
		}
		if(localObject.getLastModificationTime() > object.getLastModificationTime()) {
			return;
		}
		SkyowalletAPI.register(object);
	}
	
	/**
	 * Runs a MySQL sync.
	 * <br>Thanks to http://stackoverflow.com/a/10951183/3608831.
	 * 
	 * @param sender Used to send some informations, you can obtain the console with <b>Bukkit.getConsoleSender()</b>.
	 * <br>Set it to <b>null</b> if you want to disable the sending of informations.
	 * @param prefix The prefix to send before informations to the <b>CommandSender</b>.
	 * 
	 * @throws SQLException If an error occurs with a MySQL statement.
	 */
	
	private final void mySQLSync(final UUID localUUID) throws SQLException {
		openMySQLConnection();
		executeUpdate(MYSQL_CREATE_TABLE);
		final HashMap<UUID, SkyowalletAccount> remoteAccounts = new HashMap<UUID, SkyowalletAccount>();
		final ResultSet result = executeQuery(MYSQL_SELECT + (localUUID == null ? "" : " WHERE " + MYSQL_FIELD_UUID + " LIKE CONCAT(\"%%\", UNHEX('" + localUUID.toString().replace("-", "")  + "'), \"%%\")"));
		while(result.next()) {
			final UUID uuid = Utils.uuidTryParse(Utils.uuidAddDashes(result.getString(MYSQL_FIELD_UUID)));
			if(uuid == null) {
				continue;
			}
			
			final SkyowalletBank bank = SkyowalletAPI.getBank(result.getString(MYSQL_FIELD_BANK));
			final SkyowalletBank bankRequest = SkyowalletAPI.getBank(result.getString(MYSQL_FIELD_BANK_REQUEST));
			
			final String bankName = bank == null ? null : bank.getName();
			final String bankRequestName = bank == null ? (bankRequest == null ? null : bankRequest.getName()) : null;
			
			final SkyowalletAccount remoteAccount = new SkyowalletAccount(uuid, result.getDouble(MYSQL_FIELD_WALLET), bankName, result.getDouble(MYSQL_FIELD_BANK_BALANCE), bank == null ? false : result.getBoolean(MYSQL_FIELD_IS_BANK_OWNER), bankRequestName, result.getLong(MYSQL_FIELD_LAST_MODIFICATION_TIME));
			remoteAccounts.put(uuid, remoteAccount);
		}
		for(final SkyowalletAccount account : remoteAccounts.values()) {
			final SkyowalletAccount localAccount = SkyowalletAPI.getAccount(account.getUUID());
			if(localAccount == null || localAccount.getLastModificationTime() < account.getLastModificationTime()) {
				SkyowalletAPI.registerAccount(account);
			}
		}
		for(final SkyowalletAccount account : localUUID == null ? SkyowalletAPI.getAccounts() : new SkyowalletAccount[]{SkyowalletAPI.getAccount(localUUID)}) {
			final SkyowalletAccount remoteAccount = remoteAccounts.get(account.getUUID());
			final long lastModificationTime = account.getLastModificationTime();
			if(remoteAccount == null || remoteAccount.getLastModificationTime() < lastModificationTime) {
				final SkyowalletBank bank = account.getBank();
				final SkyowalletBank bankRequest = account.getBankRequest();
				executeUpdate(MYSQL_INSERT_REQUEST, account.getUUID().toString().replace("-", ""), String.valueOf(account.getWallet()), bank == null ? "NULL" : "'" + bank.getName() + "'", String.valueOf(account.getBankBalance()), account.isBankOwner(), bankRequest == null ? "NULL" : "'" + bankRequest.getName() + "'", lastModificationTime);
			}
		}
		closeMySQLConnection();
	}
	
	/**
	 * Saves all objects.
	 * 
	 * @param directory Where to save objects.
	 * @param objects The objects.
	 * 
	 * @throws IOException If an exception occurs while trying to read a file.
	 */
	
	public final void saveObjects(final File directory, final SkyowalletObject... objects) throws IOException {
		for(final SkyowalletObject object : objects) {
			if(object == null) {
				continue;
			}
			saveObject(directory, object);
		}
	}
	
	/**
	 * Saves an object.
	 * 
	 * @param directory Where to save object.
	 * @param object The object.
	 * 
	 * @throws IOException If an exception occurs while trying to read the file.
	 */
	
	public final void saveObject(final File directory, final SkyowalletObject object) throws IOException {
		Files.write(object.toString(), new File(directory, object.getIdentifier()), Charsets.UTF_8);
	}
	
}