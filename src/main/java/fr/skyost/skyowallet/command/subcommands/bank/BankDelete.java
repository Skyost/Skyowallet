package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Map.Entry;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;

/**
 * Represents the <em>/bank delete</em> command.
 */

public class BankDelete implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"delete", "remove"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.delete";
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
			account = skyowallet.getAccountManager().get((OfflinePlayer)sender);
			if(account == null) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}
			bank = account.getBank();
		}
		else {
			if(sender instanceof Player) {
				account = skyowallet.getAccountManager().get((OfflinePlayer)sender);
			}
			bank = skyowallet.getBankManager().get(args[0]);
		}
		
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messageUnexistingBank);
			return true;
		}
		
		if(sender.hasPermission("skyowallet.admin") || (account == null ? !(sender instanceof Player) : bank.isOwner(account))) {
			final Map<SkyowalletAccount, Double> accounts = skyowallet.getBankManager().remove(bank);
			for(final Entry<SkyowalletAccount, Double> entry : accounts.entrySet()) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey().getUUID());
				if(player != null && player.isOnline()) {
					final double amount = entry.getValue();
					player.getPlayer().sendMessage(amount == -1d ? PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestDenied, new PlayerPlaceholder(sender), new BankPlaceholder(bank), new Placeholder("reason", skyowallet.getPluginMessages().messageReasonBankDeleted)) : PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageBankDeleted, sender, amount, bank));
				}
			}
			sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		}
		else {
			sender.sendMessage(skyowallet.getPluginMessages().messageBankNoPermission);
		}
		return true;
	}

}