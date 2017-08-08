package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.utils.Utils;

public class BankCreate implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"create"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.create";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<bank>";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		if(SkyowalletAPI.isBankExists(args[0])) {
			sender.sendMessage(Skyowallet.messages.message17);
			return true;
		}
		if(!Utils.isValidFileName(args[0])) {
			sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message18, new Placeholder("/name/", args[0])));
			return true;
		}
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		if(account.hasBank()) {
			sender.sendMessage(Skyowallet.messages.message24);
			return true;
		}
		final SkyowalletBank bank = SkyowalletAPI.createBank(args[0]);
		account.setBank(bank, false);
		account.setBankOwner(true);
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}