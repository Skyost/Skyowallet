package fr.skyost.skyowallet.economy.account;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.security.InvalidParameterException;
import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.EconomyObject;
import fr.skyost.skyowallet.economy.account.holder.BankBalanceHolder;
import fr.skyost.skyowallet.economy.account.holder.WalletHolder;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.event.account.StatusChangeEvent;
import fr.skyost.skyowallet.event.bank.BankChangeEvent;
import fr.skyost.skyowallet.event.bank.BankRequestEvent;

/**
 * Used to manage players' accounts.
 */

public class SkyowalletAccount extends EconomyObject {

	/**
	 * The UUID.
	 */

	private UUID uuid;

	/**
	 * The account's wallet.
	 */

	private WalletHolder wallet;

	/**
	 * The bank.
	 */

	private String bank;

	/**
	 * The account's bank balance.
	 */

	private BankBalanceHolder bankBalance;

	/**
	 * Whether this account is a bank owner.
	 */

	private boolean isBankOwner;

	/**
	 * The current bank request.
	 */

	private String bankRequest;
	
	/**
	 * Constructs a new Skyowallet account.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param uuid The uuid.
	 */
	
	protected SkyowalletAccount(final Skyowallet skyowallet, final UUID uuid) {
		this(skyowallet, uuid, skyowallet.getPluginConfig().defaultWallet, null, 0d, false, null, false, -1L);
		updateLastModificationTime();
	}

	/**
	 * Constructs a new Skyowallet account.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param json The JSON String.
	 */

	protected SkyowalletAccount(final Skyowallet skyowallet, final String json) {
		super(skyowallet, json);
	}
	
	/**
	 * Private constructor used to synchronize accounts.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param uuid The uuid.
	 * @param wallet The account's wallet.
	 * @param bank The account's bank.
	 * @param bankBalance The account's bank balance.
	 * @param isBankOwner If the player is an owner of his bank.
	 * @param bankRequest The bank that this player is asking to join.
	 * @param isDeleted Whether this accounts will be deleted at the next synchronization.
	 * @param lastModificationTime The last modification time of the specified account.
	 */
	
	protected SkyowalletAccount(final Skyowallet skyowallet, final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final String bankRequest, final boolean isDeleted, final long lastModificationTime) {
		super(skyowallet, isDeleted, lastModificationTime);
		this.uuid = uuid;
		this.wallet = new WalletHolder(this, skyowallet.getEconomyOperations().round(wallet));
		this.bank = bank;
		this.bankBalance = new BankBalanceHolder(this, skyowallet.getEconomyOperations().round(bankBalance));
		this.isBankOwner = isBankOwner;
		this.bankRequest = bankRequest;
	}
	
	@Override
	public final String getIdentifier() {
		return uuid.toString();
	}
	
	/**
	 * Returns the UUID (as String).
	 * 
	 * @return The UUID.
	 */
	
	public UUID getUUID() {
		return uuid;
	}
	
	/**
	 * Returns the wallet.
	 * 
	 * @return The wallet.
	 */
	
	public WalletHolder getWallet() {
		return wallet;
	}

	/**
	 * Returns the account's bank.
	 * 
	 * @return The bank.
	 */
	
	public SkyowalletBank getBank() {
		if(!hasBank()) {
			return null;
		}
		return getSkyowallet().getBankManager().get(bank);
	}
	
	/**
	 * Checks if the account has a bank.
	 * 
	 * @return <b>true</b> If the account has a bank.
	 * <b>false</b> Otherwise.
	 */
	
	public boolean hasBank() {
		return bank != null;
	}
	
	/**
	 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
	 * <br>The player's wallet will be set to the current wallet + its bank balance. The bank balance will be set to 0.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * 
	 * @return The old bank balance.
	 */
	
	public double setBank(final SkyowalletBank bank) {
		return setBank(bank, true);
	}
	
	/**
	 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
	 * <br>The player's wallet will be set to the current wallet + its bank balance. The bank balance will be set to 0.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * @param round If you want to round the account total wallet.
	 * 
	 * @return The old bank balance.
	 * <br>-1.0 will be returned if the event is cancelled or if this account is already in the specified bank.
	 */
	
	public double setBank(SkyowalletBank bank, final boolean round) {
		if(hasBank() && (bank != null && this.bank.equals(bank.getName()))) {
			return -1d;
		}
		final BankChangeEvent event = new BankChangeEvent(this, bank);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return -1d;
		}
		bank = event.getNewBank();
		final double balance = bankBalance.getAmount(false);
		if(bank == null) {
			setBankOwner(false);
			this.bank = null;
		}
		else {
			this.bank = bank.getName();
		}

		if(balance > 0d) {
			bankBalance.transfer(wallet, balance, 0d, round);
		}

		getSkyowallet().getSyncManager().getMainSyncQueue().addToQueue(this);
		return round ? getSkyowallet().getEconomyOperations().round(balance) : balance;
	}
	
	/**
	 * Returns the account's bank balance.
	 * 
	 * @return The account's current bank balance.
	 */
	
	public BankBalanceHolder getBankBalance() {
		return bankBalance;
	}
	
	/**
	 * Checks if the specified account is an owner of its bank.
	 *
	 * @return <b>true</b> If the specified account is an owner of its bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public boolean isBankOwner() {
		return isBankOwner;
	}
	
	/**
	 * Sets if this account should be an owner of its bank.
	 * 
	 * @param isOwner <b>true</b> If this account should be an owner of its bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public void setBankOwner(final boolean isOwner) {
		if(!hasBank()) {
			return;
		}
		final StatusChangeEvent event = new StatusChangeEvent(this, isOwner);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		isBankOwner = event.getNewStatus();
		updateLastModificationTime();
	}
	
	/**
	 * Returns the current bank request.
	 * 
	 * @return The current bank request.
	 */
	
	public SkyowalletBank getBankRequest() {
		if(!hasBankRequest()) {
			return null;
		}
		return getSkyowallet().getBankManager().get(bankRequest);
	}
	
	/**
	 * Checks if this account has a bank request.
	 * 
	 * @return Whether this account has a bank request.
	 */
	
	public boolean hasBankRequest() {
		return bankRequest != null;
	}
	
	/**
	 * Sets the bank that this player is asking to join.
	 * 
	 * @param bank The bank that this player is asking to join.
	 */
	
	public void setBankRequest(SkyowalletBank bank) {
		if(hasBank() || (bank != null && hasBankRequest())) {
			return;
		}
		final BankRequestEvent event = new BankRequestEvent(this, bank);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		bank = event.getBank();
		bankRequest = bank == null ? null : bank.getName();
		updateLastModificationTime();
	}

	@Override
	public final void applyFromObject(final EconomyObject object) {
		if(!(object instanceof SkyowalletAccount)) {
			return;
		}

		final SkyowalletAccount account = (SkyowalletAccount)object;
		uuid = account.uuid;
		wallet = account.wallet;
		bank = account.bank;
		bankBalance = account.bankBalance;
		isBankOwner = account.isBankOwner;
		bankRequest = account.bankRequest;
		super.applyFromObject(object);
	}

	@Override
	public final void fromJSON(final String json) {
		final JSONObject object = (JSONObject)JSONValue.parse(json);
		if(!object.containsKey("uuid")) {
			throw new InvalidParameterException("\"uuid\" field must be present !");
		}

		uuid = UUID.fromString(object.get("uuid").toString());
		wallet = new WalletHolder(this, (Double)object.getOrDefault("wallet", 0d));
		bank = (String)object.getOrDefault("bank", null);
		bankBalance = new BankBalanceHolder(this, (Double)object.getOrDefault("bankBalance", 0d));
		isBankOwner = (Boolean)object.getOrDefault("isBankOwner", false);
		bankRequest = (String)object.getOrDefault("bankRequest", null);
		isDeleted = (Boolean)object.getOrDefault("isDeleted", false);
		lastModificationTime = (Long)object.getOrDefault("lastModificationTime", System.currentTimeMillis());
	}

	@Override
	public final JSONObject toJSON() {
		final JSONObject object = super.toJSON();
		object.put("uuid", uuid.toString());
		object.put("wallet", wallet.getAmount());
		object.put("bank", bank);
		object.put("bankBalance", bankBalance.getAmount());
		object.put("isBankOwner", isBankOwner);
		object.put("bankRequest", bankRequest);
		return object;
	}

}