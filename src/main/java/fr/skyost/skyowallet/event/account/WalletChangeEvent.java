package fr.skyost.skyowallet.event.account;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.AmountChangeEvent;

/**
 * Represents an event that is triggered when a wallet changes.
 */

public class WalletChangeEvent extends AmountChangeEvent {

	/**
	 * Creates a new wallet change event.
	 *
	 * @param account The account.
	 * @param newWallet The new wallet.
	 */

	public WalletChangeEvent(final SkyowalletAccount account, final double newWallet) {
		super(account, newWallet);
	}

	@Override
	public final double getOldAmount() {
		return getAccount().getWallet().getAmount();
	}

}