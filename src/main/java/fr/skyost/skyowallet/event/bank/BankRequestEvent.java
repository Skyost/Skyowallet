package fr.skyost.skyowallet.event.bank;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.event.EconomyEvent;

/**
 * Represents an event triggered when an account request a bank.
 */

public class BankRequestEvent extends EconomyEvent {

	/**
	 * The bank.
	 */
	
	private final SkyowalletBank bank;

	/**
	 * Creates a new bank request event instance.
	 *
	 * @param account The account.
	 * @param bank The bank.
	 */

	public BankRequestEvent(final SkyowalletAccount account, final SkyowalletBank bank) {
		super(account);
		this.bank = bank;
	}
	
	/**
	 * Returns the related bank.
	 * 
	 * @return The related bank.
	 */
	
	public final SkyowalletBank getBank() {
		return bank;
	}

}