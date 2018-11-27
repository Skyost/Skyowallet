package fr.skyost.skyowallet.sync;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.config.PluginConfig;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.connection.MySQLConnection;
import fr.skyost.skyowallet.sync.connection.SQLiteConnection;
import fr.skyost.skyowallet.sync.handler.SkyowalletAccountHandler;
import fr.skyost.skyowallet.sync.handler.SkyowalletBankHandler;
import fr.skyost.skyowallet.sync.handler.SkyowalletResultSetHandler;
import fr.skyost.skyowallet.sync.queue.FullSyncQueue;
import fr.skyost.skyowallet.sync.queue.SyncQueue;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This helper class allows to synchronize data of the plugin with MySQL.
 */

public class SyncManager {

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * MySQL connection instance.
	 */

	private MySQLConnection mySQLConnection;

	/**
	 * SQLite connection instance.
	 */

	private SQLiteConnection sqLiteConnection;

	/**
	 * The account result set handler.
	 */

	private SkyowalletResultSetHandler<SkyowalletAccount> accountHandler;

	/**
	 * The bank result set handler.
	 */

	private SkyowalletResultSetHandler<SkyowalletBank> bankHandler;

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

		final PluginConfig config = skyowallet.getPluginConfig();
		mySQLConnection = new MySQLConnection(config);
		sqLiteConnection = new SQLiteConnection(config);

		accountHandler = new SkyowalletAccountHandler(this);
		bankHandler = new SkyowalletBankHandler(this);

		final CommandSender sender = config.syncSilent ? null : Bukkit.getConsoleSender();

		mainQueue = config.syncSmart ? new SyncQueue(this, sender) : new FullSyncQueue(this, sender);
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
		syncQueue.synchronize();
	}

	public MySQLConnection getMySQLConnection() {
		return mySQLConnection;
	}

	public void setMySQLConnection(final MySQLConnection mySQLConnection) {
		this.mySQLConnection = mySQLConnection;
	}

	public SQLiteConnection getSQLiteConnection() {
		return sqLiteConnection;
	}

	public void setSQLiteConnection(final SQLiteConnection sqLiteConnection) {
		this.sqLiteConnection = sqLiteConnection;
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
	 * Returns the main sync queue.
	 *
	 * @return The main sync queue.
	 */

	public SyncQueue getMainSyncQueue() {
		return mainQueue;
	}

}