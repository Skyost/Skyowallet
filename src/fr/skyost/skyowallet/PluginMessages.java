package fr.skyost.skyowallet;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;

import fr.skyost.skyowallet.utils.Skyoconfig;

public class PluginMessages extends Skyoconfig {
	
	@ConfigOptions(name = "messages.1")
	public String message1 = ChatColor.RED + "You do not have the permission to run that command.";
	@ConfigOptions(name = "messages.2")
	public String message2 = ChatColor.RED + "Please perform this command from the game !";
	@ConfigOptions(name = "messages.3")
	public String message3 = ChatColor.RED + "This player does not exist or does not have any account !";
	@ConfigOptions(name = "messages.4")
	public String message4 = ChatColor.AQUA + "Welcome !/n/You have /amount/ /currency-name/ in your wallet.";
	@ConfigOptions(name = "messages.5")
	public String message5 = "Total accounts : /total-accounts/";
	@ConfigOptions(name = "messages.6")
	public String message6 = "Total money (on the server) : /amount/ /currency-name/";
	@ConfigOptions(name = "messages.7")
	public String message7 = "Best wallet : /amount/ /currency-name/";
	@ConfigOptions(name = "messages.8")
	public String message8 = ChatColor.RED + "You do not have enough money !";
	@ConfigOptions(name = "messages.9")
	public String message9 = ChatColor.GREEN + "/player/ paid you /amount/ /currency-name/ !";
	@ConfigOptions(name = "messages.10")
	public String message10 = ChatColor.GREEN + "Done.";
	@ConfigOptions(name = "messages.11")
	public String message11 = "/player/ just set your wallet at /amount/ /currency-name/ !";
	@ConfigOptions(name = "messages.12")
	public String message12 = "/player/ has /amount/ /currency-name/ in his wallet.";
	@ConfigOptions(name = "messages.13")
	public String message13 = ChatColor.RED + "This is not a valid amount !";
	@ConfigOptions(name = "messages.14")
	public String message14 = ChatColor.AQUA + "You are an owner of /bank/.";
	@ConfigOptions(name = "messages.15")
	public String message15 = ChatColor.AQUA + "Your bank is /bank/. The owners of your bank are /owners/.";
	@ConfigOptions(name = "messages.16")
	public String message16 = "You have /amount/ /currency-name/ on your bank account.";
	@ConfigOptions(name = "messages.17")
	public String message17 = ChatColor.RED + "This bank already exists !";
	@ConfigOptions(name = "messages.18")
	public String message18 = ChatColor.RED + "/bank/ is not a valid name !";
	@ConfigOptions(name = "messages.19")
	public String message19 = ChatColor.RED + "No bank exists with the given name !";
	@ConfigOptions(name = "messages.20")
	public String message20 = ChatColor.RED + "You bank (/bank/) has been deleted. /amount/ /currency-name/ were transfered in your wallet.";
	@ConfigOptions(name = "messages.21")
	public String message21 = ChatColor.RED + "You do not have a bank account.";
	@ConfigOptions(name = "messages.22")
	public String message22 = "You have /members/ members in your bank.";
	@ConfigOptions(name = "messages.23")
	public String message23 = "There are /members/ members in the specified bank.";
	@ConfigOptions(name = "messages.24")
	public String message24 = ChatColor.RED + "You already have a bank account. You can leave your bank with /bank leave.";
	@ConfigOptions(name = "messages.25")
	public String message25 = ChatColor.GOLD + "Welcome to /bank/ !";
	@ConfigOptions(name = "messages.26")
	public String message26 = ChatColor.GREEN + "You have left your bank. /amount/ /currency-name/ were transfered in your wallet.";
	@ConfigOptions(name = "messages.27")
	public String message27 = ChatColor.GRAY + "Name : " + ChatColor.WHITE + "/bank/ " + ChatColor.GRAY + "Accounts : " + ChatColor.WHITE + "/accounts/ " + ChatColor.GRAY + "Total amount : " + ChatColor.WHITE + "/amount/";
	@ConfigOptions(name = "messages.28")
	public String message28 = ChatColor.RED + "You do not have the permission to perform this action on the specified bank !";
	@ConfigOptions(name = "messages.29")
	public String message29 = ChatColor.GOLD + "You are now an owner of the bank " + ChatColor.AQUA + "/bank/" + ChatColor.GOLD + " !";
	@ConfigOptions(name = "messages.30")
	public String message30 = ChatColor.RED + "You are not an owner of /bank/ anymore !";
	@ConfigOptions(name = "messages.31")
	public String message31 = ChatColor.RED + "This player does not have any bank !";
	@ConfigOptions(name = "messages.32")
	public String message32 = "There are /banks/ banks here.";
	
	protected PluginMessages(final File dataFolder) {
		super(new File(dataFolder, "messages.yml"), Arrays.asList("Skyowallet Messages"));
	}

}
