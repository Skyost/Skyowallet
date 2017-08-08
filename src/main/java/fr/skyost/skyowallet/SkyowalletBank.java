package fr.skyost.skyowallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.Utils;

/**
 * Used to handle banks.
 */

public class SkyowalletBank {
	
	private String name;
	private boolean approvalRequired;
	private long lastModificationTime;
	
	/**
	 * Creates a new bank.
	 * 
	 * @param name The bank's name.
	 */
	
	public SkyowalletBank(final String name) {
		this(name, Skyowallet.config.banksRequireApproval, System.currentTimeMillis());
	}
	
	/**
	 * Creates a new bank.
	 * 
	 * @param name The bank's name.
	 * @param approvalRequired Whether an approval is required to join this bank.
	 */
	
	protected SkyowalletBank(final String name, final boolean approvalRequired, final long lastModificationTime) {
		this.name = name;
		this.approvalRequired = approvalRequired;
		this.lastModificationTime = lastModificationTime;
	}
	
	/**
	 * Gets the bank's name.
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
		setApprovalRequired(approvalRequired, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets whether an approval is required to join this bank.
	 * 
	 * @param approvalRequired Whether an approval is required to join this bank.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setApprovalRequired(final boolean approvalRequired, final boolean sync) {
		this.approvalRequired = approvalRequired;
		lastModificationTime = System.currentTimeMillis();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync));
		}
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
	 * Gets the owners of this bank.
	 * 
	 * @return The owners.
	 */
	
	public final SkyowalletAccount[] getOwners() {
		final List<SkyowalletAccount> owners = new ArrayList<SkyowalletAccount>();
		for(final SkyowalletAccount account : SkyowalletAPI.getAccounts()) {
			if(!isOwner(account)) {
				continue;
			}
			owners.add(account);
		}
		return owners.toArray(new SkyowalletAccount[owners.size()]);
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
	 * Gets the bank's members.
	 * 
	 * @return An HashMap containing the bank's members.
	 * <br><b>Key :</b> The member's account.
	 * <br><b>Value :</b> The member's bank balance.
	 */
	
	public final HashMap<SkyowalletAccount, Double> getMembers() {
		final HashMap<SkyowalletAccount, Double> members = new HashMap<SkyowalletAccount, Double>();
		for(final SkyowalletAccount account : SkyowalletAPI.getAccounts()) {
			if(!isMember(account)) {
				continue;
			}
			members.put(account, account.getBankBalance());
		}
		return members;
	}
	
	/**
	 * Gets the pending bank's members.
	 * 
	 * @return An array containing the pending bank's members.
	 */
	
	public final SkyowalletAccount[] getPendingMembers() {
		final HashSet<SkyowalletAccount> members = new HashSet<SkyowalletAccount>();
		for(final SkyowalletAccount account : SkyowalletAPI.getAccounts()) {
			if(!isAwaitingApproval(account)) {
				continue;
			}
			members.add(account);
		}
		return members.toArray(new SkyowalletAccount[members.size()]);
	}
	
	/**
	 * Gets the last modification time in millis of the bank.
	 * 
	 * @return The last modification time.
	 */
	
	public final long getLastModificationTime() {
		return lastModificationTime;
	}
	
	@Override
	public final String toString() {
		final JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("approvalRequired", approvalRequired);
		json.put("lastModificationTime", lastModificationTime);
		return json.toJSONString();
	}
	
	/**
	 * Constructs an instance from a JSON String.
	 * 
	 * @param json The JSON String.
	 * 
	 * @return A new instance of this class.
	 * 
	 * @throws ParseException If an error occurred while parsing the data.
	 */
	
	public static final SkyowalletBank fromJSON(final String json) throws ParseException {
		final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
		
		final Object name = jsonObject.get("name");
		if(name == null) {
			throw new IllegalArgumentException("Name is null.");
		}
		
		Object approvalRequired = jsonObject.get("approvalRequired");
		if(approvalRequired == null) {
			approvalRequired = Skyowallet.config.banksRequireApproval;
		}
		else {
			approvalRequired = Utils.booleanTryParse(approvalRequired.toString());
			if(approvalRequired == null) {
				approvalRequired = Skyowallet.config.banksRequireApproval;
			}
		}
		
		Object lastModificationTime = jsonObject.get("lastModificationTime");
		if(lastModificationTime == null) {
			lastModificationTime = 0l;
		}
		
		return new SkyowalletBank(name.toString(), (Boolean)approvalRequired, Utils.longTryParse(lastModificationTime.toString()));
	}

}