package fr.skyost.skyowallet.economy.account.holder;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.AmountChangeEvent;
import fr.skyost.skyowallet.event.account.WalletChangeEvent;

/**
 * Represents an account's wallet.
 */

public class WalletHolder extends MoneyHolder {

	/**
	 * Creates a new wallet holder instance.
	 */

	protected WalletHolder() {
		super();
	}

	/**
	 * Creates a new wallet holder instance.
	 *
	 * @param account The Skyowallet account this holder belongs to.
	 */

	public WalletHolder(final SkyowalletAccount account) {
		this(account, account.getSkyowallet().getPluginConfig().defaultWallet);
	}

	/**
	 * Creates a new wallet holder instance.
	 *
	 * @param account The Skyowallet account this holder belongs to.
	 * @param amount Default amount.
	 */

	public WalletHolder(final SkyowalletAccount account, final double amount) {
		super(account, amount);
	}

	@Override
	AmountChangeEvent createChangeEvent(final double newAmount) {
		return new WalletChangeEvent(getAccount(), newAmount);
	}

}
