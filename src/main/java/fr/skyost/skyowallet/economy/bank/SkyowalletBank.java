package fr.skyost.skyowallet.economy.bank;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;

/**
 * Used to handle banks.
 */

public class SkyowalletBank extends EconomyObject {

	/**
	 * The bank's name.
	 */
	
	private String name;

	/**
	 * Whether an approval is required.
	 */

	private boolean approvalRequired;
	
	/**
	 * Creates a new bank.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param name The bank's name.
	 */

	protected SkyowalletBank(final Skyowallet skyowallet, final String name) {
		this(skyowallet, name, skyowallet.getPluginConfig().banksRequireApproval, false, -1L);
		updateLastModificationTime();
	}

	/**
	 * Creates a new bank.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param json The JSON string.
	 * @param diff Used to differentiate from the other constructor.
	 */

	protected SkyowalletBank(final Skyowallet skyowallet, final String json, final boolean diff) {
		super(skyowallet, json);
	}
	
	/**
	 * Creates a new bank.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param name The bank's name.
	 * @param isDeleted Whether this bank will be deleted at the next synchronization.
	 * @param approvalRequired Whether an approval is required to join this bank.
	 */
	
	protected SkyowalletBank(final Skyowallet skyowallet, final String name, final boolean approvalRequired, final boolean isDeleted, final long lastModificationTime) {
		super(skyowallet, isDeleted, lastModificationTime);
		this.name = name;
		this.approvalRequired = approvalRequired;
	}
	
	@Override
	public final String getIdentifier() {
		return name;
	}

	/**
	 * Returns the bank's name.
	 * 
	 * @return The bank's name.
	 */
	
	public final String getName() {
		return name;
	}
	
	/**
	 * Sets whether an approval is required to join this bank.
	 * 
	 * @param approvalRequired Whether an approval is required to join this bank.
	 */
	
	public final void setApprovalRequired(final boolean approvalRequired) {
		this.approvalRequired = approvalRequired;
		updateLastModificationTime();
	}
	
	/**
	 * Checks whether an approval is required to join this bank.
	 * 
	 * @return Whether an approval is required to join this bank.
	 */
	
	public final boolean isApprovalRequired() {
		return approvalRequired;
	}
	
	/**
	 * Checks if the specified account is an owner of this bank.
	 * 
	 * @param account The account.
	 * 
	 * @return <b>true</b> If the specified account is an owner of this bank.
	 * <b>false</b> Otherwise.
	 */
	
	public final boolean isOwner(final SkyowalletAccount account) {
		return isMember(account) && account.isBankOwner();
	}
	
	/**
	 * Returns the owners of this bank.
	 * 
	 * @return The owners.
	 */
	
	public final SkyowalletAccount[] getOwners() {
		final List<SkyowalletAccount> owners = new ArrayList<>();
		for(final SkyowalletAccount account : getSkyowallet().getAccountManager().list()) {
			if(!isOwner(account)) {
				continue;
			}
			owners.add(account);
		}
		return owners.toArray(new SkyowalletAccount[0]);
	}
	
	/**
	 * Checks if the specified account is a member of this bank.
	 * 
	 * @param account The account.
	 * 
	 * @return <b>true</b> If the account is a member of this bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final boolean isMember(final SkyowalletAccount account) {
		final SkyowalletBank bank = account.getBank();
		return bank != null && bank.getName().equals(name);
	}
	
	/**
	 * Checks if the specified account is asking to become a member of this bank.
	 * 
	 * @param account The account.
	 * 
	 * @return <b>true</b> If the account is asking to become a member of this bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final boolean isAwaitingApproval(final SkyowalletAccount account) {
		final SkyowalletBank bank = account.getBankRequest();
		return bank != null && bank.getName().equals(name);
	}
	
	/**
	 * Returns the bank's members.
	 * 
	 * @return An HashMap containing the bank's members.
	 * <br><b>Key :</b> The member's account.
	 * <br><b>Value :</b> The member's bank balance.
	 */
	
	public final HashMap<SkyowalletAccount, Double> getMembers() {
		final HashMap<SkyowalletAccount, Double> members = new HashMap<>();
		for(final SkyowalletAccount account : getSkyowallet().getAccountManager().list()) {
			if(!isMember(account)) {
				continue;
			}
			members.put(account, account.getBankBalance().getAmount());
		}
		return members;
	}
	
	/**
	 * Returns the pending bank's members.
	 * 
	 * @return An array containing the pending bank's members.
	 */
	
	public final SkyowalletAccount[] getPendingMembers() {
		final HashSet<SkyowalletAccount> members = new HashSet<>();
		for(final SkyowalletAccount account : getSkyowallet().getAccountManager().list()) {
			if(!isAwaitingApproval(account)) {
				continue;
			}
			members.add(account);
		}
		return members.toArray(new SkyowalletAccount[0]);
	}

	@Override
	public final void applyFromObject(final EconomyObject object) {
		if(!(object instanceof SkyowalletBank)) {
			return;
		}

		final SkyowalletBank bank = (SkyowalletBank)object;
		name = bank.name;
		approvalRequired = bank.approvalRequired;
		super.applyFromObject(object);
	}

	@Override
	public final void fromJSON(final String json) {
		final JSONObject object = (JSONObject)JSONValue.parse(json);
		if(!object.containsKey("name")) {
			throw new InvalidParameterException("\"name\" field must be present !");
		}

		name = object.get("name").toString();
		approvalRequired = (Boolean)object.getOrDefault("approvalRequired", getSkyowallet().getPluginConfig().banksRequireApproval);
		isDeleted = (Boolean)object.getOrDefault("isDeleted", false);
		lastModificationTime = (Long)object.getOrDefault("lastModificationTime", System.currentTimeMillis());
	}

	@Override
	public final JSONObject toJSON() {
		final JSONObject object = super.toJSON();
		object.put("name", name);
		object.put("approvalRequired", approvalRequired);
		return object;
	}

}