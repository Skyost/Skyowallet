package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletAPI.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class BankRemoveOwner implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"removeowner", "remove-owner"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.removeowner";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<player | uuid> [bank]";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		final SkyowalletBank bank;
		if(args.length == 1) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}
			bank = SkyowalletAPI.getAccount((OfflinePlayer)sender).getBank();
			if(bank == null) {
				sender.sendMessage(Skyowallet.messages.message21);
				if(sender.hasPermission("skyowallet.admin")) {
					sender.sendMessage(Skyowallet.messages.message29);
				}
				return true;
			}
		}
		else {
			bank = SkyowalletAPI.getBank(args[1]);
			if(bank == null) {
				sender.sendMessage(Skyowallet.messages.message19);
				return true;
			}
		}
		if(!sender.hasPermission("skyowallet.admin") || sender instanceof Player ? !bank.isOwner(SkyowalletAPI.getAccount((OfflinePlayer)sender)) : false) {
			sender.sendMessage(Skyowallet.messages.message28);
			return true;
		}
		if(bank.isOwner(account)) {
			bank.removeOwner(account);
			if(player.isOnline()) {
				player.getPlayer().sendMessage(Skyowallet.messages.message31.replace("/bank/", bank.getName()));
			}
		}
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}
