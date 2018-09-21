package fr.skyost.skyowallet.economy.account;

import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.SkyowalletFactory;
import fr.skyost.skyowallet.economy.SkyowalletManager;

/**
 * Allows to create accounts.
 */

public class SkyowalletAccountFactory extends SkyowalletFactory<SkyowalletAccount, UUID> {

	/**
	 * Creates a new Skyowallet account factory instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletAccountFactory(final Skyowallet skyowallet) {
		super(skyowallet);
	}

	@Override
	public final SkyowalletAccount createFromJSON(final String json) {
		return new SkyowalletAccount(getSkyowallet(), json);
	}

	@Override
	public SkyowalletAccount create(final UUID uuid) {
		if(uuid == null) {
			return null;
		}
		return new SkyowalletAccount(getSkyowallet(), uuid);
	}

	/**
	 * Creates a new account.
	 *
	 * @param uuid The UUID.
	 * @param wallet The wallet.
	 * @param bank The bank.
	 * @param bankBalance The bank balance.
	 * @param isBankOwner Whether this account is a bank owner.
	 * @param bankRequest The requested bank.
	 * @param lastModificationTime The last modification time.
	 * @param isDeleted Whether this account will be deleted at the next synchronization.
	 *
	 * @return The account.
	 */

	public SkyowalletAccount create(final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final String bankRequest, final boolean isDeleted, final long lastModificationTime) {
		if(uuid == null) {
			return null;
		}
		return new SkyowalletAccount(getSkyowallet(), uuid, wallet, bank, bankBalance, isBankOwner, bankRequest, isDeleted, lastModificationTime);
	}

	/**
	 * Creates a new account.
	 *
	 * @param uuid The UUID.
	 * @param wallet The wallet.
	 * @param bank The bank.
	 * @param bankBalance The bank balance.
	 * @param isBankOwner Whether this account is a bank owner.
	 * @param bankRequest The requested bank.
	 * @param lastModificationTime The last modification time.
	 * @param isDeleted Whether this account will be deleted at the next synchronization.
	 * @param accountManager The account manager.
	 *
	 * @return The account.
	 */

	public SkyowalletAccount create(final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final String bankRequest, final boolean isDeleted, final long lastModificationTime, final SkyowalletManager<SkyowalletAccount> accountManager) {
		final SkyowalletAccount account = create(uuid, wallet, bank, bankBalance, isBankOwner, bankRequest, isDeleted, lastModificationTime);
		if(account != null) {
			accountManager.add(account);
		}

		return account;
	}

}