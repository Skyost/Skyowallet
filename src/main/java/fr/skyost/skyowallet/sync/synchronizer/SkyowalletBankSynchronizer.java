package fr.skyost.skyowallet.sync.synchronizer;

import com.google.common.base.Joiner;
import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.connection.DatabaseConnection;

import java.util.Set;

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
		super(skyowallet.getSyncManager().getBankHandler(), skyowallet.getBankManager(), skyowallet.getBankFactory());
	}

	@Override
	public String handleIdentifier(final DatabaseConnection connection, final SkyowalletBank bank) {
		return "`name`='" + bank.getIdentifier().replace("'", "\\'") + "'";
	}

	@Override
	public String buildSelectQuery(final DatabaseConnection connection, final Set<String> whereClause) {
		return connection.getSelectBanksRequest() + " WHERE " + Joiner.on(" OR ").join(whereClause);
	}

	@Override
	public DatabaseQuery buildUpdateQuery(final DatabaseConnection connection, final SkyowalletBank bank) {
		return new DatabaseQuery(connection.getInsertBanksRequest(), bank.getName(), bank.isApprovalRequired(), bank.isDeleted(), bank.getLastModificationTime());
	}

	@Override
	public DatabaseQuery buildDeleteQuery(final DatabaseConnection connection, final Set<String> whereClause) {
		return new DatabaseQuery(connection.getDeleteBanksRequest() + " WHERE " + Joiner.on(" OR ").join(whereClause));
	}

}
