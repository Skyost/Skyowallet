package fr.skyost.skyowallet.sync;

import org.apache.commons.dbutils.QueryRunner;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.config.PluginConfig;
import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.handler.SkyowalletAccountHandler;
import fr.skyost.skyowallet.sync.handler.SkyowalletBankHandler;
import fr.skyost.skyowallet.sync.handler.SkyowalletResultSetHandler;
import fr.skyost.skyowallet.sync.queue.SyncQueue;

/**
 * This helper class allows to synchronize data of the plugin with MySQL.
 */

public class SyncManager {
	
	/**
	 * The MySQL table that contains accounts.
	 */
	
	public static final String MYSQL_TABLE_ACCOUNTS = "skyowallet_accounts_v6";

	/**
	 * The MySQL table that contains banks.
	 */

	public static final String MYSQL_TABLE_BANKS = "skyowallet_banks_v6";
	
	/**
	 * The MySQL query that allows to create accounts' table.
	 */

	public static final String MYSQL_CREATE_TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS `" + MYSQL_TABLE_ACCOUNTS + "` (`uuid` BINARY(16) NOT NULL COMMENT 'UUID of the player, inserted with UNHEX(...) and with dashes removed.', `wallet` DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Wallet of the player.', `bank` VARCHAR(30) COMMENT 'Bank name of the player (or NULL if no bank).', `bank_balance` DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Bank balance of the player.', `is_bank_owner` BOOLEAN NOT NULL DEFAULT false COMMENT '0 if the player is an owner of its bank, 1 otherwise. The bank field must not be NULL if you want to change this field.', `bank_request` VARCHAR(30) COMMENT 'Name of the bank this player requested to join. The bank must be NULL if you want to change this field.', `is_deleted` BOOLEAN NOT NULL COMMENT 'Whether this account will be deleted at the next synchronization.', `last_modification_time` BIGINT NOT NULL COMMENT 'The elapsed time since January 1st 1970 in milliseconds. MUST BE UPDATED AFTER EACH CHANGE !', PRIMARY KEY(`uuid`))";
	
	/**
	 * The MySQL query that selects necessary data from the accounts' table.
	 */

	public static final String MYSQL_SELECT_ACCOUNTS = "SELECT HEX(`uuid`) AS `uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `bank_request`, `is_deleted`, `last_modification_time` FROM `" + MYSQL_TABLE_ACCOUNTS + "`";
	
	/**
	 * The MySQL query that inserts data to the accounts' table.
	 */

	public static final String MYSQL_INSERT_ACCOUNTS = "INSERT INTO `" + MYSQL_TABLE_ACCOUNTS + "` VALUES(UNHEX(?), ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `wallet`=VALUES(`wallet`), `bank`=VALUES(`bank`), `bank_balance`=VALUES(`bank_balance`), `is_bank_owner`=VALUES(`is_bank_owner`), `bank_request`=VALUES(`bank_request`), `is_deleted`=VALUES(`is_deleted`), `last_modification_time`=VALUES(`last_modification_time`)";

	/**
	 * The MySQL query that delete data from the accounts' table.
	 */

	public static final String MYSQL_DELETE_ACCOUNTS = "DELETE FROM `" + MYSQL_TABLE_ACCOUNTS + "`";

	/**
	 * The MySQL query that selects necessary data from the banks' table.
	 */

	public static final String MYSQL_CREATE_TABLE_BANKS = "CREATE TABLE IF NOT EXISTS " + MYSQL_TABLE_BANKS + " (`name` VARCHAR(30) NOT NULL COMMENT 'The name of the bank. If you update this field, you must change it in the accounts table as well.', `is_approval_required` BOOLEAN NOT NULL DEFAULT false COMMENT '0 if an approval is required, 1 otherwise.', `is_deleted` BOOLEAN NOT NULL COMMENT 'Whether this account will be deleted at the next synchronization.', `last_modification_time` BIGINT NOT NULL COMMENT 'The elapsed time since January 1st 1970 in milliseconds. MUST BE UPDATED AFTER EACH CHANGE !', PRIMARY KEY(`name`))";

	/**
	 * The MySQL query that selects necessary data from the banks' table.
	 */

	public static final String MYSQL_SELECT_BANKS = "SELECT * FROM " + MYSQL_TABLE_BANKS;

	/**
	 * The MySQL query that inserts data to the banks' table.
	 */

	public static final String MYSQL_INSERT_BANKS = "INSERT INTO " + MYSQL_TABLE_BANKS + " VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `is_approval_required`=VALUES(`is_approval_required`), `is_deleted`=VALUES(`is_deleted`), `last_modification_time`=VALUES(`last_modification_time`)";

	/**
	 * The MySQL query that delete data from the banks' table.
	 */

	public static final String MYSQL_DELETE_BANKS = "DELETE FROM `" + MYSQL_TABLE_BANKS + "`";

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * Whether MySQL is enabled.
	 */
	
	private boolean mySQL;

	/**
	 * The account result set handler.
	 */

	private SkyowalletResultSetHandler<SkyowalletAccount> accountHandler;

	/**
	 * The bank result set handler.
	 */

	private SkyowalletResultSetHandler<SkyowalletBank> bankHandler;
	
	/**
	 * The current MySQL connection.
	 */
	
	private Connection connection;

	/**
	 * The main sync queue.
	 */

	private SyncQueue mainQueue;

	/**
	 * Creates a new synchronization manager instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SyncManager(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;

		mainQueue = new SyncQueue(this, skyowallet.getPluginConfig().syncSilent ? null : Bukkit.getConsoleSender());
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public final Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public final void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the account result set handler.
	 *
	 * @return The account result set handler.
	 */

	public final SkyowalletResultSetHandler<SkyowalletAccount> getAccountHandler() {
		return accountHandler;
	}

	/**
	 * Sets the account result set handler.
	 *
	 * @param accountHandler The account result set handler.
	 */

	public final void setAccountHandler(final SkyowalletResultSetHandler<SkyowalletAccount> accountHandler) {
		this.accountHandler = accountHandler;
	}

	/**
	 * Returns the bank result set handler.
	 *
	 * @return The bank result set handler.
	 */

	public final SkyowalletResultSetHandler<SkyowalletBank> getBankHandler() {
		return bankHandler;
	}

	/**
	 * Sets the bank result set handler.
	 *
	 * @param bankHandler The bank result set handler.
	 */

	public final void setBankHandler(final SkyowalletResultSetHandler<SkyowalletBank> bankHandler) {
		this.bankHandler = bankHandler;
	}
	
	/**
	 * Enables MySQL.
	 *
	 * @throws SQLException If an exception occurs while creating required tables.
	 */
	
	public void enableMySQL() throws SQLException {
		mySQL = true;
		accountHandler = new SkyowalletAccountHandler(this);
		bankHandler = new SkyowalletBankHandler(this);

		openMySQLConnection();
		executeUpdate(MYSQL_CREATE_TABLE_ACCOUNTS);
		executeUpdate(MYSQL_CREATE_TABLE_BANKS);
		closeMySQLConnection();
	}
	
	/**
	 * Disables MySQL.
	 * 
	 * @throws SQLException If an exception occurs while closing the MySQL connection.
	 */
	
	public final void disableMySQL() throws SQLException {
		closeMySQLConnection();
		mySQL = false;
		accountHandler = null;
		bankHandler = null;
	}

	/**
	 * Executes an update on the MySQL database.
	 *
	 * @param request The request.
	 * @param parameters The formatting parameters.
	 *
	 * @return The result of the update.
	 *
	 * @throws SQLException If any SQL error occurs.
	 */
	
	public final int executeUpdate(final String request, final Object... parameters) throws SQLException {
		openMySQLConnection();
		return new QueryRunner().update(connection, request, parameters);
	}

	/**
	 * Executes a query on the MySQL database.
	 *
	 * @param request The request.
	 * @param handler The result set handler.
	 * @param parameters The formatting parameters.
	 *
	 * @param <T> Type of the Skyowallet ResultSet handler.
	 *
	 * @return The result of the query.
	 *
	 * @throws SQLException If any SQL error occurs.
	 */
	
	public final <T extends EconomyObject> Set<T> executeQuery(final String request, final SkyowalletResultSetHandler<T> handler, final Object... parameters) throws SQLException {
		openMySQLConnection();
		return new QueryRunner().query(connection, request, handler, parameters);
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
		final PluginConfig config = skyowallet.getPluginConfig();
		connection = DriverManager.getConnection("jdbc:mysql://" + config.mySQLHost + ":" + config.mySQLPort + "/" + config.mySQLDB + "?useSSL=false", config.mySQLUser, config.mySQLPassword);
	}
	
	/**
	 * Checks if a MySQL connection is currently opened.
	 * 
	 * @return Whether a MySQL connection is currently opened.
	 * 
	 * @throws SQLException If an exception occurs while executing <b>statement.isClosed()</b>.
	 */
	
	public final boolean isMySQLConnectionClosed() throws SQLException {
		return connection == null || connection.isClosed();
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
		connection.close();
		connection = null;
	}

	/**
	 * Synchronizes the main sync queue.
	 *
	 * @throws IOException If any I/O exception occurs.
	 * @throws SQLException If any SQL exception occurs.
	 */

	public synchronized final void synchronize() throws IOException, SQLException {
		synchronize(mainQueue);
	}

	/**
	 * Synchronizes a sync queue.
	 *
	 * @param syncQueue The sync queue.
	 *
	 * @throws IOException If any I/O exception occurs.
	 * @throws SQLException If any SQL exception occurs.
	 */

	public synchronized final void synchronize(final SyncQueue syncQueue) throws IOException, SQLException {
		if(syncQueue.size() == 0) {
			return;
		}

		openMySQLConnection();
		syncQueue.synchronize();
		closeMySQLConnection();
	}

	/**
	 * Returns the main sync queue.
	 *
	 * @return The main sync queue.
	 */

	public SyncQueue getMainSyncQueue() {
		return mainQueue;
	}

}