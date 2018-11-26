package fr.skyost.skyowallet.sync.connection;

import fr.skyost.skyowallet.config.PluginConfig;

import java.sql.SQLException;

/**
 * Represents a SQLite connection.
 */

public class SQLiteConnection extends DatabaseConnection {

	/**
	 * The SQLite query that allows to create accounts' table.
	 */

	public static final String SQLITE_CREATE_TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS `" + SQL_TABLE_ACCOUNTS + "` (`uuid` VARCHAR(32) NOT NULL, `wallet` DOUBLE NOT NULL DEFAULT 0.0, `bank` VARCHAR(30), `bank_balance` DOUBLE NOT NULL DEFAULT 0.0, `is_bank_owner` BOOLEAN NOT NULL DEFAULT false, `bank_request` VARCHAR(30), `is_deleted` BOOLEAN NOT NULL, `last_modification_time` BIGINT NOT NULL, PRIMARY KEY(`uuid`))";

	/**
	 * The SQLite query that selects necessary data from the accounts table.
	 */

	public static final String SQLITE_SELECT_ACCOUNTS = "SELECT * FROM `" + SQL_TABLE_ACCOUNTS + "`";

	/**
	 * The SQLite query that inserts data to the accounts table.
	 */

	public static final String SQLITE_INSERT_ACCOUNTS = "REPLACE INTO `" + SQL_TABLE_ACCOUNTS + "` (`uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `bank_request`, `is_deleted`, `last_modification_time`) VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * The SQLite query that delete data from the accounts table.
	 */

	public static final String SQLITE_DELETE_ACCOUNTS = "DELETE FROM `" + SQL_TABLE_ACCOUNTS + "`";

	/**
	 * The SQLite query that selects necessary data from the banks' table.
	 */

	public static final String SQLITE_CREATE_TABLE_BANKS = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_BANKS + " (`name` VARCHAR(30) NOT NULL, `is_approval_required` BOOLEAN NOT NULL DEFAULT false, `is_deleted` BOOLEAN NOT NULL, `last_modification_time` BIGINT NOT NULL, PRIMARY KEY(`name`))";

	/**
	 * The SQLite query that selects necessary data from the banks table.
	 */

	public static final String SQLITE_SELECT_BANKS = "SELECT * FROM " + SQL_TABLE_BANKS;

	/**
	 * The SQLite query that inserts data to the banks table.
	 */

	public static final String SQLITE_INSERT_BANKS = "REPLACE INTO " + SQL_TABLE_BANKS + " (`name`, `is_approval_required`, `is_deleted`, `last_modification_time`) VALUES(?, ?, ?, ?)";

	/**
	 * The SQLite query that delete data from the banks table.
	 */

	public static final String SQLITE_DELETE_BANKS = "DELETE FROM `" + SQL_TABLE_BANKS + "`";

	/**
	 * Creates a new SQLite connection instance.
	 *
	 * @param config The plugin configuration.
	 */

	public SQLiteConnection(final PluginConfig config) {
		super("jdbc:sqlite:" + config.getDatabaseFile().getPath());
	}

	/**
	 * Formats the current database.
	 *
	 * @throws SQLException If an exception occurs while creating required tables.
	 */

	public void formatDatabase() throws SQLException {
		executeUpdate(SQLITE_CREATE_TABLE_ACCOUNTS);
		executeUpdate(SQLITE_CREATE_TABLE_BANKS);
		close();
	}

	@Override
	public String getSelectAccountsRequest() {
		return SQLITE_SELECT_ACCOUNTS;
	}

	@Override
	public String getInsertAccountsRequest() {
		return SQLITE_INSERT_ACCOUNTS;
	}

	@Override
	public String getDeleteAccountsRequest() {
		return SQLITE_DELETE_ACCOUNTS;
	}

	@Override
	public String getSelectBanksRequest() {
		return SQLITE_SELECT_BANKS;
	}

	@Override
	public String getInsertBanksRequest() {
		return SQLITE_INSERT_BANKS;
	}

	@Override
	public String getDeleteBanksRequest() {
		return SQLITE_DELETE_BANKS;
	}

}
