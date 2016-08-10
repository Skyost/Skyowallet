package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;

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
		if(!account.hasBank()) {
			sender.sendMessage(Skyowallet.messages.message21);
			return true;
		}
		final double amount = account.setBank(null);
		if(amount >= 0) {
			sender.sendMessage(Skyowallet.messages.message26.replace("/amount/", String.valueOf(amount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(amount)));
		}
		return true;
	}

}
