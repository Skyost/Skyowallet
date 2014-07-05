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
	public String message10 = ChatColor.GREEN + "Done !";
	@ConfigOptions(name = "messages.11")
	public String message11 = "/player/ just set your wallet at /amount/ /currency-name/ !";
	@ConfigOptions(name = "messages.12")
	public String message12 = "/player/ has /amount/ /currency-name/ in his wallet.";
	@ConfigOptions(name = "messages.13")
	public String message13 = ChatColor.RED + "This is not a valid amount !";
	
	protected PluginMessages(final File dataFolder) {
		super(new File(dataFolder, "messages.yml"), Arrays.asList("Skyowallet Messages"));
	}

}
