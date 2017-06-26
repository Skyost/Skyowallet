package fr.skyost.skyowallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

/**
 * Used to handle banks.
 */

public class SkyowalletBank {
	
	private String name;
	
	/**
	 * Creates a new bank.
	 * 
	 * @param name The bank's name.
	 */
	
	public SkyowalletBank(final String name) {
		this.name = name;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public final String toString() {
		final JSONObject json = new JSONObject();
		json.put("name", name);
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
			throw new IllegalArgumentException("Name cannot be null.");
		}
		return new SkyowalletBank(name.toString());
	}

}