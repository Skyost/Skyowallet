package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;

public class EconomyEvent extends SkyowalletEvent {
	
	private final SkyowalletAccount account;
	
	EconomyEvent(final SkyowalletAccount account) {
		this.account = account;
	}
	
	/**
	 * Gets the account involved in this event.
	 * 
	 * @return The account.
	 */
	
	public final SkyowalletAccount getAccount() {
		return account;
	}

}
