package fr.skyost.skyowallet.commands.subcommands.bank;

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
import fr.skyost.skyowallet.utils.Utils;

public class BankSetOwner implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"setowner", "set-owner"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.setowner";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<player | uuid>";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount(player);
		final SkyowalletBank bank = account.getBank();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message31);
			return true;
		}
		if(!sender.hasPermission("skyowallet.admin") || (sender instanceof Player ? !bank.isOwner(SkyowalletAPI.getAccount((OfflinePlayer)sender)) : false)) {
			sender.sendMessage(Skyowallet.messages.message28);
			return true;
		}
		if(!bank.isOwner(account)) {
			account.setBankOwner(true);
			if(player.isOnline()) {
				player.getPlayer().sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message29, new BankPlaceholder(bank)));
			}
		}
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}