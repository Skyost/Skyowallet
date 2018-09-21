package fr.skyost.skyowallet.event;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;

/**
 * Represents an event that involves an account.
 */

public class EconomyEvent extends SkyowalletEvent {

	/**
	 * The involved account.
	 */
	
	private final SkyowalletAccount account;

	/**
	 * Creates a new economy event instance.
	 *
	 * @param account The involved account.
	 */
	
	protected EconomyEvent(final SkyowalletAccount account) {
		this.account = account;
	}
	
	/**
	 * Returns the account involved in this event.
	 * 
	 * @return The involved account.
	 */
	
	public final SkyowalletAccount getAccount() {
		return account;
	}

}