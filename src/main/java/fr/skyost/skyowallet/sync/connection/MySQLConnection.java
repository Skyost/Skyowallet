package fr.skyost.skyowallet.sync.connection;

import fr.skyost.skyowallet.config.PluginConfig;

/**
 * Represents a MySQL connection.
 */

public class MySQLConnection extends DatabaseConnection {

	/**
	 * The MySQL query that allows to create accounts table.
	 */

	public static final String MYSQL_CREATE_TABLE_ACCOUNTS = "CREATE TABLE IF NOT EXISTS `" + SQL_TABLE_ACCOUNTS + "` (`uuid` BINARY(16) NOT NULL COMMENT 'UUID of the player, inserted with UNHEX(...) and with dashes removed.', `wallet` DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Wallet of the player.', `bank` VARCHAR(30) COMMENT 'Bank name of the player (or NULL if no bank).', `bank_balance` DOUBLE NOT NULL DEFAULT 0.0 COMMENT 'Bank balance of the player.', `is_bank_owner` BOOLEAN NOT NULL DEFAULT false COMMENT '0 if the player is an owner of its bank, 1 otherwise. The bank field must not be NULL if you want to change this field.', `bank_request` VARCHAR(30) COMMENT 'Name of the bank this player requested to join. The bank must be NULL if you want to change this field.', `is_deleted` BOOLEAN NOT NULL COMMENT 'Whether this account will be deleted at the next synchronization.', `last_modification_time` BIGINT NOT NULL COMMENT 'The elapsed time since January 1st 1970 in milliseconds. MUST BE UPDATED AFTER EACH CHANGE !', PRIMARY KEY(`uuid`))";

	/**
	 * The MySQL query that selects necessary data from the accounts table.
	 */

	public static final String MYSQL_SELECT_ACCOUNTS = "SELECT HEX(`uuid`) AS `uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `bank_request`, `is_deleted`, `last_modification_time` FROM `" + SQL_TABLE_ACCOUNTS + "`";

	/**
	 * The MySQL query that inserts data to the accounts table.
	 */

	public static final String MYSQL_INSERT_ACCOUNTS = "REPLACE INTO `" + SQL_TABLE_ACCOUNTS + "` (`uuid`, `wallet`, `bank`, `bank_balance`, `is_bank_owner`, `bank_request`, `is_deleted`, `last_modification_time`) VALUES(UNHEX(?), ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * The MySQL query that delete data from the accounts table.
	 */

	public static final String MYSQL_DELETE_ACCOUNTS = "DELETE FROM `" + SQL_TABLE_ACCOUNTS + "`";

	/**
	 * The MySQL query that selects necessary data from the banks table.
	 */

	public static final String MYSQL_CREATE_TABLE_BANKS = "CREATE TABLE IF NOT EXISTS " + SQL_TABLE_BANKS + " (`name` VARCHAR(30) NOT NULL COMMENT 'The name of the bank. If you update this field, you must change it in the accounts table as well.', `is_approval_required` BOOLEAN NOT NULL DEFAULT false COMMENT '0 if an approval is required, 1 otherwise.', `is_deleted` BOOLEAN NOT NULL COMMENT 'Whether this account will be deleted at the next synchronization.', `last_modification_time` BIGINT NOT NULL COMMENT 'The elapsed time since January 1st 1970 in milliseconds. MUST BE UPDATED AFTER EACH CHANGE !', PRIMARY KEY(`name`))";

	/**
	 * The MySQL query that selects necessary data from the banks table.
	 */

	public static final String MYSQL_SELECT_BANKS = "SELECT * FROM " + SQL_TABLE_BANKS;

	/**
	 * The MySQL query that inserts data to the banks table.
	 */

	public static final String MYSQL_INSERT_BANKS = "REPLACE INTO " + SQL_TABLE_BANKS + " (`name`, `is_approval_required`, `is_deleted`, `last_modification_time`) VALUES(?, ?, ?, ?)";

	/**
	 * The MySQL query that delete data from the banks table.
	 */

	public static final String MYSQL_DELETE_BANKS = "DELETE FROM `" + SQL_TABLE_BANKS + "`";

	/**
	 * Creates a new MySQL connection instance.
	 *
	 * @param config The plugin configuration.
	 */

	public MySQLConnection(final PluginConfig config) {
		super("jdbc:mysql://" + config.mySQLHost + ":" + config.mySQLPort + "/" + config.mySQLDB + "?useSSL=false", config.mySQLUser, config.mySQLPassword);
	}

	@Override
	public String getCreateAccountsTableRequest() {
		return MYSQL_CREATE_TABLE_ACCOUNTS;
	}

	@Override
	public String getSelectAccountsRequest() {
		return MYSQL_SELECT_ACCOUNTS;
	}

	@Override
	public String getInsertAccountsRequest() {
		return MYSQL_INSERT_ACCOUNTS;
	}

	@Override
	public String getDeleteAccountsRequest() {
		return MYSQL_DELETE_ACCOUNTS;
	}

	@Override
	public String getCreateBanksTableRequest() {
		return MYSQL_CREATE_TABLE_BANKS;
	}

	@Override
	public String getSelectBanksRequest() {
		return MYSQL_SELECT_BANKS;
	}

	@Override
	public String getInsertBanksRequest() {
		return MYSQL_INSERT_BANKS;
	}

	@Override
	public String getDeleteBanksRequest() {
		return MYSQL_DELETE_BANKS;
	}

}
