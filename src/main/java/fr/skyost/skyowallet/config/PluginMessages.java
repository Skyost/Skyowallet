package fr.skyost.skyowallet.config;

import org.bukkit.ChatColor;

import java.io.File;
import java.util.Collections;

import fr.skyost.skyowallet.util.Skyoconfig;

/**
 * Allows to configure plugin's messages.
 */

public class PluginMessages extends Skyoconfig {
	
	@ConfigOptions(name = "messages.no-permission")
	public String messageNoPermission = ChatColor.RED + "You do not have the permission to run this command.";
	@ConfigOptions(name = "messages.console-disallowed")
	public String messageConsoleDisallowed = ChatColor.RED + "Please perform this command from the game !";
	@ConfigOptions(name = "messages.player-no-account")
	public String messagePlayerNoAccount = ChatColor.RED + "This player does not exist or does not have an account on this server !";
	@ConfigOptions(name = "messages.welcome")
	public String messageWelcome = ChatColor.AQUA + "Welcome /player/ !/n/You have /amount/ /currency-name/ in your wallet.";
	@ConfigOptions(name = "messages.total-accounts")
	public String messageTotalAccounts = "Total accounts : /total-accounts/";
	@ConfigOptions(name = "messages.total-money")
	public String messageTotalMoney = "Total money (on the server) : /amount/ /currency-name/";
	@ConfigOptions(name = "messages.best-amount")
	public String messageBestAmount = "Best amount of money : /amount/ /currency-name/ by /player/";
	@ConfigOptions(name = "messages.not-enough-money")
	public String messageNotEnoughMoney = ChatColor.RED + "You do not have enough money !";
	@ConfigOptions(name = "messages.amount-paid")
	public String messageAmountPaid = ChatColor.GREEN + "/player/ paid you /amount/ /currency-name/ !";
	@ConfigOptions(name = "messages.done")
	public String messageDone = ChatColor.GREEN + "Done.";
	@ConfigOptions(name = "messages.wallet-set")
	public String messageWalletSet = "/player/ just set your wallet to /amount/ /currency-name/ !";
	@ConfigOptions(name = "messages.wallet-info")
	public String messageWalletInfo = "/player/ has /amount/ /currency-name/ in his wallet.";
	@ConfigOptions(name = "messages.invalid-amount")
	public String messageInvalidAmount = ChatColor.RED + "This is not a valid amount !";
	@ConfigOptions(name = "messages.bank-owner")
	public String messageBankOwner = ChatColor.AQUA + "You are an owner of /bank/.";
	@ConfigOptions(name = "messages.bank-member")
	public String messageBankMember = ChatColor.AQUA + "Your bank is /bank/. The owners of your bank are /owners/.";
	@ConfigOptions(name = "messages.bank-info")
	public String messageBankInfo = "You have /amount/ /currency-name/ in your bank account.";
	@ConfigOptions(name = "messages.bank-already-exists")
	public String messageBankAlreadyExists = ChatColor.RED + "This bank already exists !";
	@ConfigOptions(name = "messages.invalid-name")
	public String messageInvalidName = ChatColor.RED + "/name/ is not a valid name !";
	@ConfigOptions(name = "messages.unexisting-bank")
	public String messageUnexistingBank = ChatColor.RED + "No bank exists with the given name !";
	@ConfigOptions(name = "messages.bank-deleted")
	public String messageBankDeleted = ChatColor.RED + "Your bank (/bank/) has been deleted. /amount/ /currency-name/ were transferred to your wallet.";
	@ConfigOptions(name = "messages.no-bank-account")
	public String messageNoBankAccount = ChatColor.RED + "You do not have a bank account (if you asked to join a bank and your request has not been approved yet, you can cancel it with " + ChatColor.GRAY + "/bank cancel" + ChatColor.RED + ").";
	@ConfigOptions(name = "messages.own-bank-members")
	public String messageOwnBankMembers = "You have /members/ members in your bank.";
	@ConfigOptions(name = "messages.bank-members")
	public String messageBankMembers = "There are /members/ members in the specified bank.";
	@ConfigOptions(name = "messages.already-have-bank")
	public String messageAlreadyHaveBank = ChatColor.RED + "You already have a bank account. You can leave your bank with " + ChatColor.GRAY + "/bank leave" + ChatColor.RED + ".";
	@ConfigOptions(name = "messages.bank-welcome")
	public String messageBankWelcome = ChatColor.GOLD + "Welcome to /bank/ !";
	@ConfigOptions(name = "messages.bank-left")
	public String messageBankLeft = ChatColor.GREEN + "You have left your bank. /amount/ /currency-name/ were transferred to your wallet.";
	@ConfigOptions(name = "messages.bank-ranking")
	public String messageBankRanking = "- " + ChatColor.GRAY + "Name : " + ChatColor.WHITE + "/bank/ " + ChatColor.GRAY + "Accounts : " + ChatColor.WHITE + "/accounts/ " + ChatColor.GRAY + "Total amount : " + ChatColor.WHITE + "/amount/ /currency-name/";
	@ConfigOptions(name = "messages.bank-no-permission")
	public String messageBankNoPermission = ChatColor.RED + "You do not have the permission to perform this action on the specified bank !";
	@ConfigOptions(name = "messages.bank-owner-added")
	public String messageBankOwnerAdded = ChatColor.GOLD + "You are now an owner of the bank " + ChatColor.AQUA + "/bank/" + ChatColor.GOLD + " !";
	@ConfigOptions(name = "messages.bank-owner-removed")
	public String messageBankOwnerRemoved = ChatColor.RED + "You are not an owner of /bank/ anymore !";
	@ConfigOptions(name = "messages.player-no-bank")
	public String messagePlayerNoBank = ChatColor.RED + "This player does not have any bank !";
	@ConfigOptions(name = "messages.bank-count")
	public String messageBankCount = "There are /banks/ banks here.";
	@ConfigOptions(name = "messages.no-account")
	public String messageNoAccount = ChatColor.RED + "You do not have an economy account on this server.";
	@ConfigOptions(name = "messages.bank-request")
	public String messageBankRequest = ChatColor.GREEN + "You have successfully requested to join /bank/. The owners will approve or deny your request.";
	@ConfigOptions(name = "messages.bank-not-requested")
	public String messageBankNotRequested = ChatColor.RED + "You do not have requested to join a bank.";
	@ConfigOptions(name = "messages.player-bank-request")
	public String messagePlayerBankRequest = ChatColor.AQUA + "/player/ wants to join your bank. You can accept his request with " + ChatColor.GOLD + "/bank approve <player | uuid>" + ChatColor.AQUA + "./n/You can deny it with " + ChatColor.GOLD + "/bank deny <player | uuid> [reason]" + ChatColor.AQUA + ".";
	@ConfigOptions(name = "messages.player-bank-not-requested")
	public String messagePlayerBankNotRequested = ChatColor.RED + "The player did not have requested to join a bank.";
	@ConfigOptions(name = "messages.bank-request-cancelled")
	public String messageBankRequestCancelled = ChatColor.RED + "You have cancelled your request of joining /bank/.";
	@ConfigOptions(name = "messages.bank-request-accepted")
	public String messageBankRequestAccepted = ChatColor.GREEN + "/player/ has accepted your request of joining the bank /bank/ !";
	@ConfigOptions(name = "messages.bank-request-denied")
	public String messageBankRequestDenied = ChatColor.RED + "/player/ has denied your request of joining the bank /bank/ (/reason/).";
	@ConfigOptions(name = "messages.no-reason")
	public String messageNoReason = "No reason";
	@ConfigOptions(name = "messages.reason-bank-deleted")
	public String messageReasonBankDeleted = "The bank has been deleted";
	@ConfigOptions(name = "messages.bank-request-count")
	public String messageBankRequestCount = "You have currently /members/ members that are willing to join your bank.";
	@ConfigOptions(name = "messages.bank-request-already-sent")
	public String messageBankRequestAlreadySent = ChatColor.RED + "You already have asked to join /bank/./n/You can cancel your request with " + ChatColor.GRAY + "/bank cancel" + ChatColor.RED + ".";
	@ConfigOptions(name = "messages.invalid-integer")
	public String messageInvalidInteger = ChatColor.RED + "This is not a valid integer (must also be strictly positive) !";
	@ConfigOptions(name = "messages.account-ranking")
	public String messageAccountRanking = "- " + ChatColor.GRAY + "Name : " + ChatColor.WHITE + "/player/ " + ChatColor.GRAY + "Total amount : " + ChatColor.WHITE + "/amount/ /currency-name/";
	@ConfigOptions(name = "messages.player-count")
	public String messagePlayerCount = "There are /players/ players here. They are ranked in ascending order.";
	@ConfigOptions(name = "messages.tax-earned")
	public String messageTaxEarned = "[Taxes] " + ChatColor.GOLD + "/amount/ /currency-name/ were added to your wallet / bank balance.";
	@ConfigOptions(name = "messages.taxes-rate")
	public String messageTaxesRate = ChatColor.RED + "Tax rate : /rate/%, new amount : /amount/ /currency-name/.";
	@ConfigOptions(name = "message.command-usage")
	public String messageCommandUsage = ChatColor.RED + "Usage for you :/n//usage/";

	/**
	 * Creates a new plugin messages instance.
	 *
	 * @param dataFolder The plugin's data folder.
	 */

	public PluginMessages(final File dataFolder) {
		super(new File(dataFolder, "messages.yml"), Collections.singletonList("Skyowallet Messages"));
	}

}