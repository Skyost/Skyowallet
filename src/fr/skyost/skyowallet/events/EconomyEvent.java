package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAccount;

public class EconomyEvent extends SkyowalletCancellableEvent {
	
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