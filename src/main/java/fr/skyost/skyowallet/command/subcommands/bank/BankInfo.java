package fr.skyost.skyowallet.command.subcommands.bank;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Represents the <em>/bank info</em> command.
 */

public class BankInfo implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"info"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.info";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[bank]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		SkyowalletAccount account = null;
		final SkyowalletBank bank;
		
		if(args.length < 1) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			account = skyowallet.getAccountManager().get((Player)sender);
			if(account == null) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}
			bank = account.getBank();
		}
		else {
			if(sender instanceof Player) {
				account = skyowallet.getAccountManager().get((Player)sender);
			}
			bank = skyowallet.getBankManager().get(args[0]);
		}
		
		if(bank == null) {
			sender.sendMessage(ChatColor.DARK_RED + (args.length < 1 ? skyowallet.getPluginMessages().messageNoBankAccount : skyowallet.getPluginMessages().messageUnexistingBank));
			return true;
		}
		
		if(sender.hasPermission("skyowallet.admin") || (account == null ? !(sender instanceof Player) : bank.isOwner(account))) {
			final HashMap<SkyowalletAccount, Double> members = bank.getMembers();
			for(final Entry<SkyowalletAccount, Double> entry : members.entrySet()) {
				final UUID uuid = entry.getKey().getUUID();
				final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				final Double balance = entry.getValue();
				sender.sendMessage((player == null ? uuid.toString() : Util.getName(player)) + " " + ChatColor.AQUA + balance + " " + skyowallet.getPluginConfig().getCurrencyName(balance));
			}
			sender.sendMessage(Util.SEPARATOR);
			sender.sendMessage(PlaceholderFormatter.format(args.length < 1 ? skyowallet.getPluginMessages().messageOwnBankMembers : skyowallet.getPluginMessages().messageBankMembers, new Placeholder("members", String.valueOf(members.size()))));
		}
		else {
			sender.sendMessage(skyowallet.getPluginMessages().messageBankNoPermission);
		}
		return true;
	}

}