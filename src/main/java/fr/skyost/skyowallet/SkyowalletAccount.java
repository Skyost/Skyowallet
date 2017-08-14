package fr.skyost.skyowallet;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import fr.skyost.skyowallet.events.BankBalanceChangeEvent;
import fr.skyost.skyowallet.events.BankChangeEvent;
import fr.skyost.skyowallet.events.BankRequestEvent;
import fr.skyost.skyowallet.events.StatusChangeEvent;
import fr.skyost.skyowallet.events.WalletChangeEvent;
import fr.skyost.skyowallet.tasks.SyncTask;

/**
 * Used to manage players' accounts.
 */

public class SkyowalletAccount extends SkyowalletObject {
	
	@MustBePresent
	private String uuid;
	private double wallet;
	private String bank;
	private double bankBalance;
	private boolean isBankOwner;
	private String bankRequest;
	
	/**
	 * Constructs a new Skyowallet's account.
	 * 
	 * @param json The JSON string.
	 * 
	 * @throws ParseException If an exception occurs while parsing JSON.
	 * @throws IllegalAccessException  If an exception occurs while accessing fields.
	 * @throws IllegalArgumentException If an exception occurs while reading JSON.
	 */
	
	private SkyowalletAccount(final String json) throws IllegalArgumentException, IllegalAccessException, ParseException {
		super(json);
	}
	
	/**
	 * Constructs a new Skyowallet's account.
	 * 
	 * @param uuid The uuid.
	 */
	
	public SkyowalletAccount(final UUID uuid) {
		this(uuid, 0d, null, 0d, false, null, System.currentTimeMillis());
	}
	
	/**
	 * Private constructor used to synchronize accounts.
	 * 
	 * @param uuid The uuid.
	 * @param wallet The account's wallet.
	 * @param bank The account's bank.
	 * @param bankBalance The account's bank balance.
	 * @param isBankOwner If the player is an owner of his bank.
	 * @param bankRequest The bank that this player is asking to join.
	 * @param lastModificationTime The last modification time of the specified account.
	 */
	
	protected SkyowalletAccount(final UUID uuid, final double wallet, final String bank, final double bankBalance, final boolean isBankOwner, final String bankRequest, final long lastModificationTime) {
		super(lastModificationTime);
		this.uuid = uuid.toString();
		this.wallet = SkyowalletAPI.round(wallet);
		this.bank = bank;
		this.bankBalance = SkyowalletAPI.round(bankBalance);
		this.isBankOwner = isBankOwner;
		this.bankRequest = bankRequest;
	}
	
	@Override
	public final String getIdentifier() {
		return uuid;
	}
	
	/**
	 * Gets the UUID (as String).
	 * 
	 * @return The UUID.
	 */
	
	public final UUID getUUID() {
		return UUID.fromString(uuid);
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
		setWallet(wallet, sync, round, Skyowallet.config.taxesRateGlobal);
	}
	
	/**
	 * Sets the wallet.
	 * 
	 * @param wallet The wallet.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the specified amount (will not round the amount if changed by an event).
	 * @param taxRate The tax rate for the new amount. Will be applied only if the new wallet is superior than the wallet and if the account does not have the bypass permission.
	 */
	
	public final void setWallet(double wallet, final boolean sync, final boolean round, final double taxRate) {
		if(taxRate > 0d && wallet > this.wallet) {
			final Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if(player == null || !player.hasPermission("skyowallet.taxes.bypass")) {
				wallet = this.wallet + SkyowalletAPI.tax(wallet - this.wallet, taxRate);
			}
		}
		final WalletChangeEvent event = new WalletChangeEvent(this, round ? SkyowalletAPI.round(wallet) : wallet);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		this.wallet = event.getNewWallet();
		updateLastModificationTime();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync ? null : Bukkit.getConsoleSender(), UUID.fromString(uuid)));
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
		setBankBalance(0d, false, round, 0d);
		setWallet(wallet + balance, sync, round, SkyowalletAPI.getDeleteBankTaxRate());
		return round ? SkyowalletAPI.round(balance) : balance;
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
	 * @param bankBalance The new bank balance.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setBankBalance(final double bankBalance, final boolean sync) {
		setBankBalance(bankBalance, sync, true);
	}
	
	/**
	 * Sets the account's bank balance.
	 * 
	 * @param bankBalance The new bank balance.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the specified balance (will not round the amount if changed by an event).
	 */
	
	public final void setBankBalance(final double bankBalance, final boolean sync, final boolean round) {
		setBankBalance(bankBalance, sync, round, Skyowallet.config.taxesRateGlobal);
	}
	
	/**
	 * Sets the account's bank balance.
	 * 
	 * @param bankBalance The new bank balance.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 * @param round If you want to round the specified balance (will not round the amount if changed by an event).
	 * @param taxRate The tax rate for the new amount. Will be applied only if the new bank balance is superior than the old bank balance and if the account does not have the bypass permission.
	 */
	
	public final void setBankBalance(double bankBalance, final boolean sync, final boolean round, final double taxRate) {
		if(!hasBank()) {
			return;
		}
		if(taxRate > 0d && bankBalance > this.bankBalance) {
			final Player player = Bukkit.getPlayer(UUID.fromString(uuid));
			if(player == null || !player.hasPermission("skyowallet.taxes.bypass")) {
				bankBalance = this.bankBalance + SkyowalletAPI.tax(bankBalance - this.bankBalance, taxRate);
			}
		}
		final BankBalanceChangeEvent event = new BankBalanceChangeEvent(this, round ? SkyowalletAPI.round(bankBalance) : bankBalance);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) {
			return;
		}
		this.bankBalance = event.getNewBankBalance();
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync ? null : Bukkit.getConsoleSender(), UUID.fromString(uuid)));
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
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync ? null : Bukkit.getConsoleSender(), UUID.fromString(uuid)));
		}
	}
	
	/**
	 * Gets the current bank request.
	 * 
	 * @return The current bank request.
	 */
	
	public final SkyowalletBank getBankRequest() {
		if(!hasBankRequest()) {
			return null;
		}
		return SkyowalletAPI.getBank(bankRequest);
	}
	
	/**
	 * Checks if this account has a bank request.
	 * 
	 * @return Whether this account has a bank request.
	 */
	
	public final boolean hasBankRequest() {
		return bankRequest != null;
	}
	
	/**
	 * Sets the bank that this player is asking to join.
	 * 
	 * @param bank The bank that this player is asking to join.
	 */
	
	public final void setBankRequest(final SkyowalletBank bank) {
		setBankRequest(bank, Skyowallet.config.syncEachModification);
	}
	
	/**
	 * Sets the bank that this player is asking to join.
	 * 
	 * @param bank The bank that this player is asking to join.
	 * @param sync If you want to synchronizes the database (asynchronously).
	 */
	
	public final void setBankRequest(SkyowalletBank bank, final boolean sync) {
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
		if(sync) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(SkyowalletAPI.getPlugin(), new SyncTask(Skyowallet.config.silentSync ? null : Bukkit.getConsoleSender(), UUID.fromString(uuid)));
		}
	}
	
	/**
	 * Constructs an instance from a JSON String.
	 * 
	 * @param json The JSON String.
	 * 
	 * @return A new instance of this class.
	 * 
	 * @throws ParseException If an exception occurs while parsing JSON.
	 * @throws IllegalAccessException  If an exception occurs while accessing fields.
	 * @throws IllegalArgumentException If an exception occurs while reading JSON.
	 */
	
	public static final SkyowalletAccount fromJSON(final String json) throws ParseException, IllegalArgumentException, IllegalAccessException {	
		return new SkyowalletAccount(json);
	}

}