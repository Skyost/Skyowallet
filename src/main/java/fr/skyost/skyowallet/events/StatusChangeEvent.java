package fr.skyost.skyowallet.events;

import fr.skyost.skyowallet.SkyowalletAccount;

public class StatusChangeEvent extends EconomyEvent {
	
	private boolean willBeBankOwner;

	public StatusChangeEvent(final SkyowalletAccount account, final boolean willBeBankOwner) {
		super(account);
		this.willBeBankOwner = willBeBankOwner;
	}
	
	/**
	 * Same as <b>account.isBankOwner()</b>.
	 * 
	 * @return <b>true</b> If the account is a bank owner.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final boolean getOldStatus() {
		return this.getAccount().isBankOwner();
	}
	
	/**
	 * If the account will be a bank owner.
	 * 
	 * @return <b>true</b> If the account will be a bank owner.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final boolean getNewStatus() {
		return willBeBankOwner;
	}
	
	/**
	 * Modify the new status.
	 * 
	 * @param willBeBankOwner <b>true</b> If the account will be a bank owner.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final void setNewState(final boolean willBeBankOwner) {
		this.willBeBankOwner = willBeBankOwner;
	}

}