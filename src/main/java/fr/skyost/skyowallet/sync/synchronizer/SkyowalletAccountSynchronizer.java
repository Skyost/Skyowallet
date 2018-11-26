package fr.skyost.skyowallet.sync.synchronizer;

import com.google.common.base.Joiner;
import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.sync.connection.DatabaseConnection;
import fr.skyost.skyowallet.sync.connection.MySQLConnection;

import java.util.Set;

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
		super(skyowallet.getSyncManager().getAccountHandler(), skyowallet.getAccountManager(), skyowallet.getAccountFactory());
	}

	@Override
	public String handleIdentifier(final DatabaseConnection connection, final SkyowalletAccount account) {
		boolean isMySQL = connection instanceof MySQLConnection;
		String identifier = "'" + account.getIdentifier().replace("-", "") + "'";
		if(isMySQL) {
			identifier = "UNHEX(" + identifier + ")";
		}
		return "`uuid`=" + identifier;
	}

	@Override
	public String buildSelectQuery(final DatabaseConnection connection, final Set<String> whereClause) {
		return connection.getSelectAccountsRequest() + " WHERE " + Joiner.on(" OR ").join(whereClause);
	}

	@Override
	public DatabaseQuery buildUpdateQuery(final DatabaseConnection connection, final SkyowalletAccount account) {
		return new DatabaseQuery(connection.getInsertAccountsRequest(), account.getUUID().toString().replace("-", ""), account.getWallet().getAmount(), account.getBank() == null ? null : account.getBank().getName(), account.getBankBalance().getAmount(), account.isBankOwner(), account.getBankRequest() == null ? null : account.getBankRequest().getName(), account.isDeleted(), account.getLastModificationTime());
	}

	@Override
	public DatabaseQuery buildDeleteQuery(final DatabaseConnection connection, final Set<String> whereClause) {
		return new DatabaseQuery(connection.getDeleteAccountsRequest() + " WHERE " + Joiner.on(" OR ").join(whereClause));
	}

}
