package fr.skyost.skyowallet.sync.synchronizer;

import com.google.common.base.Joiner;

import java.util.Set;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.SyncManager;

/**
 * The synchronizer that allows to synchronizer banks.
 */

public class SkyowalletBankSynchronizer extends SkyowalletSynchronizer<SkyowalletBank> {

	/**
	 * Creates a new Skyowallet account synchronizer instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletBankSynchronizer(final Skyowallet skyowallet) {
		super(
				skyowallet.getPluginConfig().getBanksDirectory(),
				SkyowalletBankSynchronizer::identifierHandler,
				SkyowalletBankSynchronizer::selectQueryBuilder,
				SkyowalletBankSynchronizer::updateQueryBuilder,
				SkyowalletBankSynchronizer::deleteQueryBuilder,
				skyowallet.getSyncManager().getBankHandler(),
				skyowallet.getBankManager(),
				skyowallet.getBankFactory()
			 );
	}

	/**
	 * The identifier handler.
	 *
	 * @param bank The bank to handle.
	 *
	 * @return The bank's identifier (for SQL queries).
	 */

	private static String identifierHandler(final SkyowalletBank bank) {
		return "`name`='" + bank.getIdentifier().replace("'", "\\'") + "'";
	}

	/**
	 * The SELECT query builder.
	 *
	 * @param whereClause The WHERE clause.
	 *
	 * @return The SELECT MySQL query.
	 */

	private static String selectQueryBuilder(final Set<String> whereClause) {
		return SyncManager.MYSQL_SELECT_BANKS + " WHERE " + Joiner.on(" OR ").join(whereClause);
	}

	/**
	 * The UPDATE query builder.
	 *
	 * @param bank The bank.
	 *
	 * @return The UPDATE MySQL query.
	 */

	private static MySQLQuery updateQueryBuilder(final SkyowalletBank bank) {
		return new MySQLQuery(SyncManager.MYSQL_INSERT_BANKS, bank.getName(), bank.isApprovalRequired(), bank.isDeleted(), bank.getLastModificationTime());
	}

	/**
	 * The DELETE query builder.
	 *
	 * @param whereClause The WHERE clause.
	 *
	 * @return The DELETE MySQL query.
	 */

	private static MySQLQuery deleteQueryBuilder(final Set<String> whereClause) {
		return new MySQLQuery(SyncManager.MYSQL_DELETE_BANKS + " WHERE " + Joiner.on(" OR ").join(whereClause));
	}

}
