package fr.skyost.skyowallet.sync.connection;

import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.sync.handler.SkyowalletResultSetHandler;
import org.apache.commons.dbutils.QueryRunner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Set;

/**
 * Represents a database connection.
 */

public abstract class DatabaseConnection {

	/**
	 * The SQL table that contains accounts.
	 */

	public static final String SQL_TABLE_ACCOUNTS = "skyowallet_accounts_v6";

	/**
	 * The SQL table that contains banks.
	 */

	public static final String SQL_TABLE_BANKS = "skyowallet_banks_v6";

	/**
	 * Database URL.
	 */

	private String url;

	/**
	 * The username.
	 */

	private String username;

	/**
	 * The password.
	 */

	private String password;

	/**
	 * The current connection.
	 */

	private Connection connection;

	/**
	 * Whether this kind of connection is enabled.
	 */

	private boolean enabled = false;

	/**
	 * Creates a new database connection instance.
	 *
	 * @param url The database URL.
	 */

	public DatabaseConnection(final String url) {
		this(url, null, null);
	}

	/**
	 * Creates a new database connection instance.
	 *
	 * @param url The database URL.
	 * @param username The username.
	 * @param password The password.
	 */

	public DatabaseConnection(final String url, final String username, final String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	/**
	 * Opens a database connection.
	 *
	 * @throws SQLException If an exception occurs while opening the database connection.
	 */

	public void open() throws SQLException {
		if(!enabled || !isClosed()) {
			return;
		}

		connection = username != null && password != null ? DriverManager.getConnection(url, username, password) : DriverManager.getConnection(url);
	}

	/**
	 * Checks if a connection is currently opened.
	 *
	 * @return Whether a connection is currently opened.
	 *
	 * @throws SQLException If an exception occurs while executing <b>connection.isClosed()</b>.
	 */

	public boolean isClosed() throws SQLException {
		return connection == null || connection.isClosed();
	}

	/**
	 * Closes the current connection.
	 *
	 * @throws SQLException If an exception occurs while closing the connection.
	 */

	public void close() throws SQLException {
		if(!enabled || isClosed()) {
			return;
		}

		connection.close();
		connection = null;
	}

	/**
	 * Returns whether this kind of connection is enabled.
	 *
	 * @return Whether this kind of connection is enabled.
	 */

	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Enables this kind of connection.
	 */

	public void enable() {
		try {
			enabled = true;

			open();
			executeUpdate(getCreateAccountsTableRequest());
			executeUpdate(getCreateBanksTableRequest());
			close();
		}
		catch(final SQLException ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Unable to enable " + getClass().getName() + " !");
			ex.printStackTrace();

			enabled = false;
		}
	}

	/**
	 * Disables this kind of connection.
	 */

	public void disable() {
		try {
			close();

			enabled = false;
		}
		catch(final SQLException ex) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Unable to disable " + getClass().getName() + " !");
			ex.printStackTrace();

			enabled = true;
		}
	}

	/**
	 * Executes an update on the database.
	 *
	 * @param request The request.
	 * @param parameters The formatting parameters.
	 *
	 * @return The result of the update.
	 *
	 * @throws SQLException If any SQL error occurs.
	 */

	public final int executeUpdate(final String request, final Object... parameters) throws SQLException {
		open();
		return new QueryRunner().update(connection, request, parameters);
	}

	/**
	 * Executes a query on the database.
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
		open();
		return new QueryRunner().query(connection, request, handler, parameters);
	}

	/**
	 * Returns the database URL.
	 *
	 * @return The database URL.
	 */

	public String getURL() {
		return url;
	}

	/**
	 * Sets the database URL.
	 *
	 * @param url The database URL.
	 */

	public void setURL(final String url) {
		this.url = url;
	}

	/**
	 * Returns the username.
	 *
	 * @return The username.
	 */

	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username The username.
	 */

	public void setUsername(final String username) {
		this.username = username;
	}

	/**
	 * Returns the password.
	 *
	 * @return The password.
	 */

	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password The password.
	 */

	public void setPassword(final String password) {
		this.password = password;
	}

	/**
	 * Returns the CREATE TABLE accounts request.
	 *
	 * @return The CREATE TABLE accounts request.
	 */

	public abstract String getCreateAccountsTableRequest();

	/**
	 * Returns the SELECT accounts request.
	 *
	 * @return The SELECT accounts request.
	 */

	public abstract String getSelectAccountsRequest();

	/**
	 * Returns the INSERT / UPDATE accounts request.
	 *
	 * @return The INSERT / UPDATE accounts request.
	 */

	public abstract String getInsertAccountsRequest();

	/**
	 * Returns the DELETE accounts request.
	 *
	 * @return The DELETE accounts request.
	 */

	public abstract String getDeleteAccountsRequest();

	/**
	 * Returns the CREATE TABLE banks request.
	 *
	 * @return The CREATE TABLE banks request.
	 */

	public abstract String getCreateBanksTableRequest();

	/**
	 * Returns the SELECT banks request.
	 *
	 * @return The SELECT banks request.
	 */

	public abstract String getSelectBanksRequest();

	/**
	 * Returns the INSERT / UPDATE banks request.
	 *
	 * @return The INSERT / UPDATE banks request.
	 */

	public abstract String getInsertBanksRequest();

	/**
	 * Returns the DELETE banks request.
	 *
	 * @return The DELETE banks request.
	 */

	public abstract String getDeleteBanksRequest();

}