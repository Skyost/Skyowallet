package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;

public class WalletChangeEvent extends SkyowalletEvent {
	
	private double newWallet;

	public WalletChangeEvent(final SkyowalletAccount account, final double newWallet) {
		super(account);
		this.newWallet = newWallet;
	}
	
	/**
	 * Same as <b>account.getWallet()</b>.
	 * 
	 * @return The old wallet.
	 */
	
	public final double getOldWallet() {
		return this.getAccount().getWallet();
	}
	
	/**
	 * Gets the new wallet.
	 * 
	 * @return The new wallet.
	 */
	
	public final double getNewWallet() {
		return newWallet;
	}
	
	/**
	 * Modify the new wallet.
	 * 
	 * @param newWallet The new wallet.
	 */
	
	public final void setNewWallet(final double newWallet) {
		this.newWallet = newWallet;
	}

}
