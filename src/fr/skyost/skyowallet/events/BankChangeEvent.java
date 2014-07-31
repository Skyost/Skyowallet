package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletBank;

public class BankChangeEvent extends SkyowalletEvent {
	
	private SkyowalletBank newBank;

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
		return this.getAccount().getBank();
	}
	
	/**
	 * Gets the new bank.
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
