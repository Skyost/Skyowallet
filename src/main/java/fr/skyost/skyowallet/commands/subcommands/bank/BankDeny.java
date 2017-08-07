package fr.skyost.skyowallet.commands.subcommands.bank;

import java.util.Arrays;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.google.common.base.Joiner;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.Utils;

public class BankDeny implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"deny"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.deny";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<player | uuid> [reason]";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		
		final SkyowalletAccount playerAccount = SkyowalletAPI.getAccount(player);
		final SkyowalletBank bank = playerAccount.getBankRequest();
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message36);
			return true;
		}
		
		if((account.isBankOwner() || account.getBank() == null || !account.getBank().equals(bank)) && !sender.hasPermission("skyowallet.admin")) {
			sender.sendMessage(Skyowallet.messages.message1);
			return true;
		}
		
		playerAccount.setBankRequest(null);
		sender.sendMessage(Skyowallet.messages.message10);
		
		if(player.isOnline()) {
			String reason = Skyowallet.messages.message40;
			if(args.length > 1) {
				reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
			}
			player.getPlayer().sendMessage(Skyowallet.messages.message39.replace("/player/", sender.getName()).replace("/bank/", bank.getName()).replace("/reason/", reason));
		}
		return true;
	}

}