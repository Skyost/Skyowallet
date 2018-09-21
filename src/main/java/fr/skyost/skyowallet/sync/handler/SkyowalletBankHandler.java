package fr.skyost.skyowallet.sync.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.economy.bank.SkyowalletBankFactory;
import fr.skyost.skyowallet.sync.SyncManager;

/**
 * Represents a result set handler that can return banks.
 */

public class SkyowalletBankHandler extends SkyowalletResultSetHandler<SkyowalletBank> {

	/**
	 * The bank factory.
	 */

	private SkyowalletBankFactory bankFactory;

	/**
	 * Creates a new skyowallet bank handler instance.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public SkyowalletBankHandler(final SyncManager syncManager) {
		super(syncManager);
	}

	@Override
	public final SkyowalletBank handleLine(final ResultSet resultSet) throws SQLException {
		return bankFactory.create(resultSet.getString("name"), resultSet.getBoolean("is_approval_required"), resultSet.getBoolean("is_deleted"), resultSet.getLong("last_modification_time"));
	}

	@Override
	public final void setSyncManager(final SyncManager syncManager) {
		super.setSyncManager(syncManager);

		this.bankFactory = syncManager.getSkyowallet().getBankFactory();
	}

}