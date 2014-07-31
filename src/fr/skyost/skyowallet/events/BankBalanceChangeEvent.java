package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;

public class BankBalanceChangeEvent extends SkyowalletEvent {
	
	private double newBankBalance;

	public BankBalanceChangeEvent(final SkyowalletAccount account, final double newBankBalance) {
		super(account);
		this.newBankBalance = newBankBalance;
	}
	
	/**
	 * Same as <b>account.getBankBalance()</b>.
	 * 
	 * @return The old bank balance.
	 */
	
	public final double getOldBankBalance() {
		return this.getAccount().getBankBalance();
	}
	
	/**
	 * Gets the new bank balance.
	 * 
	 * @return The new bank balance.
	 */
	
	public final double getNewBankBalance() {
		return newBankBalance;
	}
	
	/**
	 * Modify the new bank balance.
	 * 
	 * @param newBankBalance The new bank balance.
	 */
	
	public final void setNewBankBalance(final double newBankBalance) {
		this.newBankBalance = newBankBalance;
	}

}
