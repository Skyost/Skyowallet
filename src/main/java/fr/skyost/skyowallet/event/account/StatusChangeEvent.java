package fr.skyost.skyowallet.event.account;

import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.event.EconomyEvent;

/**
 * Represents an event triggered when an account status changes.
 */

public class StatusChangeEvent extends EconomyEvent {

	/**
	 * Whether this account will be a bank owner.
	 */
	
	private boolean willBeBankOwner;

	/**
	 * Creates a new status change event instance.
	 *
	 * @param account The account.
	 * @param willBeBankOwner Whether this account will be a bank owner.
	 */

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
		return getAccount().isBankOwner();
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