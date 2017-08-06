package fr.skyost.skyowallet;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import fr.skyost.skyowallet.events.BankBalanceChangeEvent;
import fr.skyost.skyowallet.events.BankChangeEvent;
import fr.skyost.skyowallet.events.StatusChangeEvent;
import fr.skyost.skyowallet.events.WalletChangeEvent;
import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.Utils;

/**
 * Used to manage players' accounts.
 */

public class SkyowalletAccount {
	
	private final UUID uuid;
	private double wallet;
	private String bank;
	private double bankBalance;
	private boolean isBankOwner;
	private long lastModificationTime;
	
	/**
	 * Constructs a new Skyowallet's account.
	 * 
	 * @param uuid The uuid.
	 */
	
	public SkyowalletAccount(final UUID uuid) {
		this(uuid, 0d, null, 0d, false, System.currentTimeMillis());
	}
	
	/**
	 * Private constructor used to synchronize accounts.
	 * 
	 * @param uuid The uuid.
	 * @param wallet The account's wallet.
	 * @param bank The account's bank.
	 * @param bankBalance The account's bank balance.
	 * @param isBankOwner If the player is an owner of his bank.
	 * @param lastModificationTime The last modification time of the specified account.
	 */
	
	protected SkyowalletAccount(final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final long lastModificationTime) {
		this.uuid = uuid;
		this.wallet = wallet;
		this.bank = bank;
		this.bankBalance = bankBalance;
		this.isBankOwner = isBankOwner;
		this.lastModificationTime = lastModificationTime;
	}
	
	/**
	 * Gets the UUID (as String).
	 * 
	 * @return The UUID.
	 */
	
	public final UUID getUUID() {
		return uuid;
	}
	
	/**
	 * Gets the wallet.
	 * 
	 * @return The wallet.
	 */
	
	public final double getWallet() {
		return getWallet(true);
	}
	
	/**
	 * Gets the wallet.
	 * 
	 * @param round If the bank balance should rounded.
	 * 
	 * @return The wallet.
	 */
	
	public final double getWallet(final boolean round) {
		return round ? SkyowalletAPI.round(wallet) : wallet;
	}
	
	/**
	 * Sets the wallet. The database will be synchronized if the user has enabled "sync-each-modification" in the config.
	 * 
	 * @param wallet The wallet.
	 */
	
	public final void setWallet(final double wallet) {
		setWallet(wallet, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets the wallet.
	 * 
	 * @param wallet The wallet.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setWallet(final double wallet, final boolean sync) {
		setWallet(wallet, sync, true);
	}
	
	/**
	 * Sets the wallet.
	 * 
	 * @param wallet The wallet.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the specified amount (will not round the amount if changed by an event).
	 */
	
	public final void setWallet(final double wallet, final boolean sync, final boolean round) {
		final WalletChangeEvent event = new WalletChangeEvent(this, SkyowalletAPI.round(wallet));
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		this.wallet = event.getNewWallet();
		lastModificationTime = System.currentTimeMillis();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync));
		}
	}
	
	/**
	 * Gets the account's bank.
	 * 
	 * @return The bank.
	 */
	
	public final SkyowalletBank getBank() {
		if(!hasBank()) {
			return null;
		}
		return SkyowalletAPI.getBank(bank);
	}
	
	/**
	 * Checks if the account has a bank.
	 * 
	 * @return <b>true</b> If the account has a bank.
	 * <b>false</b> Otherwise.
	 */
	
	public final boolean hasBank() {
		return bank != null;
	}
	
	/**
	 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
	 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * 
	 * @return The old bank balance.
	 */
	
	public final double setBank(final SkyowalletBank bank) {
		return setBank(bank, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * 
	 * @return The old bank balance.
	 * <br>-1.0 will be returned if the event is cancelled or if this account is already in the specified bank.
	 */
	
	public final double setBank(final SkyowalletBank bank, final boolean sync) {
		return setBank(bank, sync, true);
	}
	
	/**
	 * Sets the bank of the account. <b>null</b> if you want to clear the account's bank.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the account total wallet.
	 * 
	 * @return The old bank balance.
	 * <br>-1.0 will be returned if the event is cancelled or if this account is already in the specified bank.
	 */
	
	public final double setBank(SkyowalletBank bank, final boolean sync, final boolean round) {
		if(hasBank() && (bank != null && this.bank.equals(bank.getName()))) {
			return -1d;
		}
		final BankChangeEvent event = new BankChangeEvent(this, bank);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return -1d;
		}
		bank = event.getNewBank();
		final double balance = bankBalance;
		if(bank == null) {
			setBankOwner(false, false);
			this.bank = null;
		}
		else {
			this.bank = bank.getName();
		}
		setWallet(SkyowalletAPI.round(wallet + balance), false);
		setBankBalance(0d, sync);
		return SkyowalletAPI.round(balance);
	}
	
	/**
	 * Gets the account's bank balance.
	 * 
	 * @return The account's current bank balance.
	 */
	
	public final double getBankBalance() {
		return getBankBalance(true);
	}
	
	/**
	 * Gets the account's bank balance.
	 * 
	 * @param round If the bank balance should rounded.
	 * 
	 * @return The account's current bank balance.
	 */
	
	public final double getBankBalance(final boolean round) {
		return round ? SkyowalletAPI.round(bankBalance) : bankBalance;
	}
	
	/**
	 * Sets the account's bank balance. The database will be synchronized if the user has enabled "sync-each-modification" in the config.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 */
	
	public final void setBankBalance(final double bankBalance) {
		setBankBalance(bankBalance, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets the account's bank balance.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setBankBalance(final double bankBalance, final boolean sync) {
		setBankBalance(bankBalance, sync, true);
	}
	
	/**
	 * Sets the account's bank balance.
	 * 
	 * @param bank The bank. <b>null</b> if you want to clear the account's bank.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the specified balance (will not round the amount if changed by an event).
	 */
	
	public final void setBankBalance(final double bankBalance, final boolean sync, final boolean round) {
		if(!hasBank()) {
			return;
		}
		final BankBalanceChangeEvent event = new BankBalanceChangeEvent(this, SkyowalletAPI.round(bankBalance));
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		this.bankBalance = event.getNewBankBalance();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync));
		}
	}
	
	/**
	 * Checks if the specified account is an owner of its bank.
	 * 
	 * @param account The account.
	 * 
	 * @return <b>true</b> If the specified account is an owner of its bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final boolean isBankOwner() {
		return isBankOwner;
	}
	
	/**
	 * Sets if this account should be an owner of its bank.
	 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
	 * 
	 * @param isOwner <b>true</b> If this account should be an owner of its bank.
	 * <br><b>false</b> Otherwise.
	 */
	
	public final void setBankOwner(final boolean isOwner) {
		setBankOwner(isOwner, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets if this account should be an owner of its bank.
	 * <br>The database will be synchronized if the user has enabled "sync-each-modification" in the config.
	 * 
	 * @param isOwner <b>true</b> If this account should be an owner of its bank.
	 * <br><b>false</b> Otherwise.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setBankOwner(final boolean isOwner, final boolean sync) {
		if(!hasBank() || isBankOwner() == isOwner) {
			return;
		}
		final StatusChangeEvent event = new StatusChangeEvent(this, isOwner);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		isBankOwner = event.getNewStatus();
		lastModificationTime = System.currentTimeMillis();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync));
		}
	}
	
	/**
	 * Gets the last modification time in millis of the account.
	 * 
	 * @return The last modification time.
	 */
	
	public final long getLastModificationTime() {
		return lastModificationTime;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public final String toString() {
		final JSONObject json = new JSONObject();
		json.put("uuid", uuid.toString());
		json.put("wallet", wallet);
		json.put("bank", bank);
		json.put("bankBalance", bankBalance);
		json.put("isBankOwner", isBankOwner);
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
	
	public static final SkyowalletAccount fromJson(final String json) throws ParseException {
		final JSONObject jsonObject = (JSONObject)JSONValue.parseWithException(json);
		Object uuid = jsonObject.get("uuid");
		final Object lastModificationTime = jsonObject.get("lastModificationTime");
		if(uuid == null || lastModificationTime == null) {
			throw new NullPointerException("UUID / Last modification is null.");
		}
		uuid = Utils.uuidTryParse(uuid.toString());
		if(uuid == null) {
			throw new IllegalArgumentException("This is not a true UUID !");
		}
		Object wallet = jsonObject.get("wallet");
		if(wallet == null || Utils.doubleTryParse(wallet.toString()) == null) {
			wallet = 0.0;
		}
		final Object bank = jsonObject.get("bank");
		Object bankBalance = jsonObject.get("bankBalance");
		if(bankBalance == null || Utils.doubleTryParse(bankBalance.toString()) == null) {
			bankBalance = 0.0;
		}
		final Object isBankOwner = jsonObject.get("isBankOwner");
		return new SkyowalletAccount((UUID)uuid, Double.parseDouble(wallet.toString()), bank == null ? null : bank.toString(), Double.parseDouble(bankBalance.toString()), isBankOwner == null ? false : Boolean.valueOf(isBankOwner.toString()), Long.parseLong(lastModificationTime.toString()));
	}

}