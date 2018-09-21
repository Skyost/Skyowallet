package fr.skyost.skyowallet.event.bank;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.event.EconomyEvent;

/**
 * Represents an event triggered when an account change its bank.
 */

public class BankChangeEvent extends EconomyEvent {

	/**
	 * The new bank.
	 */
	
	private SkyowalletBank newBank;

	/**
	 * Creates a new bank change event instance.
	 *
	 * @param account The account.
	 * @param newBank The new bank.
	 */

	public BankChangeEvent(final SkyowalletAccount account, final SkyowalletBank newBank) {
		super(account);
		this.newBank = newBank;
	}
	
	/**
	 * Same as <b>account.getBank()</b>.
	 * 
	 * @return The old bank.
	 */
	
	public final SkyowalletBank getOldBank() {
		return getAccount().getBank();
	}
	
	/**
	 * Returns the new bank.
	 * 
	 * @return The new bank.
	 */
	
	public final SkyowalletBank getNewBank() {
		return newBank;
	}
	
	/**
	 * Modify the new bank.
	 * 
	 * @param newBank The new bank.
	 */
	
	public final void setNewWallet(final SkyowalletBank newBank) {
		this.newBank = newBank;
	}

}