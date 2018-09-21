package fr.skyost.skyowallet.sync.synchronizer;

import com.google.common.base.Joiner;

import java.util.Set;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.sync.SyncManager;

/**
 * The synchronizer that allows to synchronizer accounts.
 */

public class SkyowalletAccountSynchronizer extends SkyowalletSynchronizer<SkyowalletAccount> {

	/**
	 * Creates a new Skyowallet account synchronizer instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletAccountSynchronizer(final Skyowallet skyowallet) {
		super(
				skyowallet.getPluginConfig().getAccountsDirectory(),
				SkyowalletAccountSynchronizer::identifierHandler,
				SkyowalletAccountSynchronizer::selectQueryBuilder,
				SkyowalletAccountSynchronizer::updateQueryBuilder,
				SkyowalletAccountSynchronizer::deleteQueryBuilder,
				skyowallet.getSyncManager().getAccountHandler(),
				skyowallet.getAccountManager(),
				skyowallet.getAccountFactory()
			 );
	}

	/**
	 * The identifier handler.
	 *
	 * @param account The account to handle.
	 *
	 * @return The account's identifier (for SQL queries).
	 */

	private static String identifierHandler(final SkyowalletAccount account) {
		return "`uuid`=UNHEX('" + account.getIdentifier().replace("-", "") + "')";
	}

	/**
	 * The SELECT query builder.
	 *
	 * @param whereClause The WHERE clause.
	 *
	 * @return The SELECT MySQL query.
	 */

	private static String selectQueryBuilder(final Set<String> whereClause) {
		return SyncManager.MYSQL_SELECT_ACCOUNTS + " WHERE " + Joiner.on(" OR ").join(whereClause);
	}

	/**
	 * The UPDATE query builder.
	 *
	 * @param account The account.
	 *
	 * @return The UPDATE MySQL query.
	 */

	private static MySQLQuery updateQueryBuilder(final SkyowalletAccount account) {
		return new MySQLQuery(SyncManager.MYSQL_INSERT_ACCOUNTS, account.getUUID().toString().replace("-", ""), account.getWallet().getAmount(), account.getBank() == null ? null : account.getBank().getName(), account.getBankBalance().getAmount(), account.isBankOwner(), account.getBankRequest() == null ? null : account.getBankRequest().getName(), account.isDeleted(), account.getLastModificationTime());
	}

	/**
	 * The DELETE query builder.
	 *
	 * @param whereClause The WHERE clause.
	 *
	 * @return The DELETE MySQL query.
	 */

	private static MySQLQuery deleteQueryBuilder(final Set<String> whereClause) {
		return new MySQLQuery(SyncManager.MYSQL_DELETE_ACCOUNTS + " WHERE " + Joiner.on(" OR ").join(whereClause));
	}

}
