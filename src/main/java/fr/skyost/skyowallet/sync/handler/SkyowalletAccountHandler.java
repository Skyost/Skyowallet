package fr.skyost.skyowallet.sync.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import fr.skyost.skyowallet.economy.SkyowalletManager;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountFactory;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.sync.SyncManager;
import fr.skyost.skyowallet.util.Utils;

/**
 * Represents a result set handler that can return accounts.
 */

public class SkyowalletAccountHandler extends SkyowalletResultSetHandler<SkyowalletAccount> {

	/**
	 * The bank manager.
	 */

	private SkyowalletManager<SkyowalletBank> bankManager;

	/**
	 * The account factory.
	 */

	private SkyowalletAccountFactory accountFactory;

	/**
	 * Creates a new skyowallet account handler instance.
	 *
	 * @param syncManager The synchronization manager.
	 */

	public SkyowalletAccountHandler(final SyncManager syncManager) {
		super(syncManager);
	}

	@Override
	public final SkyowalletAccount handleLine(final ResultSet resultSet) throws SQLException {
		final UUID uuid = Utils.uuidTryParse(Utils.uuidAddDashes(resultSet.getString("uuid")));
		if(uuid == null) {
			return null;
		}

		final SkyowalletBank bank = bankManager.get(resultSet.getString("bank"));
		final SkyowalletBank bankRequest = bankManager.get(resultSet.getString("bank_request"));

		final String bankName = bank == null ? null : bank.getName();
		final String bankRequestName = bank == null ? (bankRequest == null ? null : bankRequest.getName()) : null;

		return accountFactory.create(uuid, resultSet.getDouble("wallet"), bankName, resultSet.getDouble("bank_balance"), bank != null && resultSet.getBoolean("is_bank_owner"), bankRequestName, resultSet.getBoolean("is_deleted"), resultSet.getLong("last_modification_time"));
	}

	@Override
	public final void setSyncManager(final SyncManager syncManager) {
		super.setSyncManager(syncManager);

		this.bankManager = syncManager.getSkyowallet().getBankManager();
		this.accountFactory = syncManager.getSkyowallet().getAccountFactory();
	}

}