package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;

public class BankRequestEvent extends EconomyEvent {
	
	private SkyowalletBank bank;

	public BankRequestEvent(final SkyowalletAccount account, final SkyowalletBank bank) {
		super(account);
		this.bank = bank;
	}
	
	/**
	 * Gets the related bank.
	 * 
	 * @return The related bank.
	 */
	
	public final SkyowalletBank getBank() {
		return bank;
	}

}