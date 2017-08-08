package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;

public class BankLeave implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"leave"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.leave";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return null;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		
		if(account == null) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		if(!account.hasBank()) {
			sender.sendMessage(Skyowallet.messages.message21);
			return true;
		}
		
		final double amount = account.setBank(null);
		sender.sendMessage(amount > 0 ? PlaceholderFormatter.defaultFormat(Skyowallet.messages.message26, sender, amount, amount) : Skyowallet.messages.message10);
		return true;
	}

}