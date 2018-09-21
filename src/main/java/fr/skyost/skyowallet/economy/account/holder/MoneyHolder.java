package fr.skyost.skyowallet.economy.account.holder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.AmountChangeEvent;

/**
 * Allows to holds an amount of money and to perform some operations on it (add, subtract, ...).
 */

public abstract class MoneyHolder {

	/**
	 * The account that holds this amount.
	 */

	private SkyowalletAccount account;

	/**
	 * The amount.
	 */

	private double amount;

	/**
	 * Creates a new money holder instance.
	 */

	protected MoneyHolder() {
		this(null);
	}

	/**
	 * Creates a new money holder instance.
	 *
	 * @param account The account this holder belongs to.
	 */

	public MoneyHolder(final SkyowalletAccount account) {
		this(account, 0d);
	}

	/**
	 * Creates a new money holder instance.
	 *
	 * @param account The account this holder belongs to.
	 * @param amount The default value.
	 */

	public MoneyHolder(final SkyowalletAccount account, final double amount) {
		this.account = account;
		this.amount = amount;
	}

	/**
	 * Returns the account that holds this amount.
	 *
	 * @return The account that holds this amount.
	 */

	public final SkyowalletAccount getAccount() {
		return account;
	}

	/**
	 * Sets the account that should hold this amount.
	 *
	 * @param account The account that should hold this amount..
	 */

	protected final void setAccount(final SkyowalletAccount account) {
		this.account = account;
	}

	/**
	 * Returns the amount.
	 *
	 * @return The amount.
	 */

	public double getAmount() {
		return getAmount(true);
	}

	/**
	 * Returns the amount.
	 *
	 * @param round Whether the amount should be rounded.
	 *
	 * @return The amount.
	 */

	public double getAmount(final boolean round) {
		return round ? account.getSkyowallet().getEconomyOperations().round(amount) : amount;
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount The amount.
	 */

	public void setAmount(final double amount) {
		setAmount(amount, account.getSkyowallet().getPluginConfig().taxesRateGlobal);
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount. Will be applied only if the new amount is superior than the specified amount and if the account does not have the bypass permission.
	 */

	public void setAmount(final double amount, final double taxRate) {
		setAmount(amount, taxRate, true);
	}

	/**
	 * Sets the amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount. Will be applied only if the new amount is superior than the specified amount and if the account does not have the bypass permission.
	 * @param round If you want to round the specified amount (will not round the amount if changed by an event).
	 */

	public void setAmount(double amount, final double taxRate, final boolean round) {
		if(taxRate > 0d && amount > this.amount) {
			final Player player = Bukkit.getPlayer(account.getUUID());
			if(player == null || !player.hasPermission("skyowallet.taxes.bypass")) {
				amount = this.amount + account.getSkyowallet().getEconomyOperations().tax(amount - this.amount, taxRate);
			}
		}

		final AmountChangeEvent event = createChangeEvent(round ? account.getSkyowallet().getEconomyOperations().round(amount) : amount);
		if(event != null) {
			Bukkit.getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				return;
			}
			amount = event.getNewAmount();
		}

		this.amount = amount;
		account.updateLastModificationTime();
	}

	/**
	 * Adds the specified amount.
	 *
	 * @param amount The amount.
	 */

	public void addAmount(final double amount) {
		addAmount(amount, account.getSkyowallet().getPluginConfig().taxesRateGlobal);
	}

	/**
	 * Adds the specified amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 */

	public void addAmount(final double amount, final double taxRate) {
		addAmount(amount, taxRate, true);
	}

	/**
	 * Adds the specified amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 * @param round If you want to round the total amount (will not round the amount if changed by an event).
	 */

	public void addAmount(final double amount, final double taxRate, final boolean round) {
		setAmount(this.amount + amount, taxRate, round);
	}

	/**
	 * Subtracts the specified amount.
	 *
	 * @param amount The amount.
	 */

	public void subtractAmount(final double amount) {
		subtractAmount(amount, account.getSkyowallet().getPluginConfig().taxesRateGlobal);
	}

	/**
	 * Subtracts the specified amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 */

	public void subtractAmount(final double amount, final double taxRate) {
		subtractAmount(amount, taxRate, true);
	}

	/**
	 * Subtracts the specified amount.
	 *
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 * @param round If you want to round the total amount (will not round the amount if changed by an event).
	 */

	public void subtractAmount(final double amount, final double taxRate, final boolean round) {
		if(!canSubtract(amount)) {
			return;
		}
		setAmount(this.amount - amount, taxRate, round);
	}

	/**
	 * Returns whether the specified amount can be subtracted from the current money.
	 *
	 * @param amount The amount.
	 *
	 * @return Whether the specified amount can be subtracted from the current money.
	 */

	public boolean canSubtract(final double amount) {
		return this.amount >= amount;
	}

	/**
	 * Transfers the specified amount of money to the specified target.
	 *
	 * @param target The target.
	 * @param amount The amount.
	 */

	public void transfer(final MoneyHolder target, final double amount) {
		transfer(target, amount, account.getSkyowallet().getPluginConfig().taxesRateGlobal);
	}

	/**
	 * Transfers the specified amount of money to the specified target.
	 *
	 * @param target The target.
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 */

	public void transfer(final MoneyHolder target, final double amount, final double taxRate) {
		transfer(target, amount, taxRate, true);
	}

	/**
	 * Transfers the specified amount of money to the specified target.
	 *
	 * @param target The target.
	 * @param amount The amount.
	 * @param taxRate The tax rate for the new amount.
	 * @param round If you want to round the total amount (will not round the amount if changed by an event).
	 */

	public void transfer(final MoneyHolder target, final double amount, final double taxRate, final boolean round) {
		if(!canSubtract(amount) || amount <= 0d) {
			return;
		}

		target.addAmount(amount, taxRate, round);
		subtractAmount(amount, 0d, round);
	}

	/**
	 * Creates a new amount change event.
	 *
	 * @param newAmount The new amount that will be applied.
	 *
	 * @return The new amount change event.
	 */

	abstract AmountChangeEvent createChangeEvent(final double newAmount);

}