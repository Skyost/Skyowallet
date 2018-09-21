package fr.skyost.skyowallet.event.bank;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.AmountChangeEvent;

/**
 * Represents an event that is triggered when a bank balance changes.
 */

public class BankBalanceChangeEvent extends AmountChangeEvent {

	/**
	 * Creates a new bank balance change event.
	 *
	 * @param account The account.
	 * @param newBankBalance The new bank balance.
	 */

	public BankBalanceChangeEvent(final SkyowalletAccount account, final double newBankBalance) {
		super(account, newBankBalance);
	}
	
	@Override
	public final double getOldAmount() {
		return getAccount().getBankBalance().getAmount();
	}

}