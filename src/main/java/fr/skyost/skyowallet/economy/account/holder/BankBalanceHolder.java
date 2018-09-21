package fr.skyost.skyowallet.economy.account.holder;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.AmountChangeEvent;
import fr.skyost.skyowallet.event.bank.BankBalanceChangeEvent;

/**
 * Represents an account's bank balance.
 */

public class BankBalanceHolder extends MoneyHolder {

	/**
	 * Creates a new bank balance holder instance.
	 */

	protected BankBalanceHolder() {
		super();
	}

	/**
	 * Creates a new bank balance holder instance.
	 *
	 * @param account The Skyowallet account this holder belongs to.
	 */

	public BankBalanceHolder(final SkyowalletAccount account) {
		super(account);
	}

	/**
	 * Creates a new bank balance holder instance.
	 *
	 * @param account The Skyowallet account this holder belongs to.
	 * @param amount Default amount.
	 */

	public BankBalanceHolder(final SkyowalletAccount account, final double amount) {
		super(account, amount);
	}

	@Override
	public final void setAmount(double amount, final double taxRate, final boolean round) {
		if(!getAccount().hasBank()) {
			return;
		}

		super.setAmount(amount, taxRate, round);
	}

	@Override
	AmountChangeEvent createChangeEvent(final double newAmount) {
		return new BankBalanceChangeEvent(getAccount(), newAmount);
	}

}
