package fr.skyost.skyowallet.event;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;

/**
 * Represents an event triggered when an amount changes.
 */

public abstract class AmountChangeEvent extends EconomyEvent {

	/**
	 * The new amount.
	 */

	private double newAmount;

	/**
	 * Creates a new amount change event instance.
	 *
	 * @param account The account.
	 * @param newAmount The new amount.
	 */

	protected AmountChangeEvent(final SkyowalletAccount account, final double newAmount) {
		super(account);

		this.newAmount = newAmount;
	}

	/**
	 * Returns the old amount.
	 *
	 * @return The old amount.
	 */

	public abstract double getOldAmount();

	/**
	 * Returns the new amount.
	 *
	 * @return The new amount.
	 */

	public final double getNewAmount() {
		return newAmount;
	}

	/**
	 * Modify the new amount.
	 *
	 * @param newAmount The new amount.
	 */

	public final void setNewAmount(final double newAmount) {
		this.newAmount = newAmount;
	}

}