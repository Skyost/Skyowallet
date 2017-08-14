package fr.skyost.skyowallet.commands.subcommands.bank;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.PlayerPlaceholder;

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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		SkyowalletAccount account = null;
		final SkyowalletBank bank;
		
		if(args.length < 1) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
			if(account == null) {
				sender.sendMessage(Skyowallet.messages.message33);
				return true;
			}
			bank = account.getBank();
		}
		else {
			if(sender instanceof Player) {
				account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
			}
			bank = SkyowalletAPI.getBank(args[0]);
		}
		
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message19);
			return true;
		}
		
		if(sender.hasPermission("skyowallet.admin") || (account == null ? !(sender instanceof Player) : bank.isOwner(account))) {
			final HashMap<SkyowalletAccount, Double> accounts = SkyowalletAPI.deleteBank(bank);
			for(final Entry<SkyowalletAccount, Double> entry : accounts.entrySet()) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey().getUUID());
				if(player != null && player.isOnline()) {
					final double amount = entry.getValue();
					player.getPlayer().sendMessage(amount == -1d ? PlaceholderFormatter.format(Skyowallet.messages.message40, new PlayerPlaceholder(sender), new BankPlaceholder(bank), new Placeholder("/reason/", Skyowallet.messages.message42)) : PlaceholderFormatter.defaultFormat(Skyowallet.messages.message20, sender, bank, amount, amount));
				}
			}
			sender.sendMessage(Skyowallet.messages.message10);
		}
		else {
			sender.sendMessage(Skyowallet.messages.message28);
		}
		return true;
	}

}