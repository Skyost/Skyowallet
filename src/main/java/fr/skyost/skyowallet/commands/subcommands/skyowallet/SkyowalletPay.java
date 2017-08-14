package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SyncManager;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.Utils;

public class SkyowalletPay implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"pay", "give"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.pay";
	}

	@Override
	public final int getMinArgsLength() {
		return 2;
	}

	@Override
	public final String getUsage() {
		return "<amount> <player | uuid>";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		
		final OfflinePlayer player = Utils.getPlayerByArgument(args[1]);
		if(player == null || !SkyowalletAPI.hasAccount(player)) {
			sender.sendMessage(Skyowallet.messages.message3);
			return true;
		}
		
		final Double amount = SkyowalletAPI.round(Utils.doubleTryParse(args[0]));
		if(amount == null) {
			sender.sendMessage(Skyowallet.messages.message13);
			return true;
		}
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		
		final SkyowalletAccount playerAccount = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		final double wallet = playerAccount.getWallet() - amount;
		if(wallet < 0d) {
			sender.sendMessage(Skyowallet.messages.message8);
			return true;
		}
		playerAccount.setWallet(wallet, false);
		
		final SkyowalletAccount targetAccount = SkyowalletAPI.getAccount(player);
		final double targetWallet = targetAccount.getWallet() + amount;
		targetAccount.setWallet(targetWallet, SyncManager.shouldSyncEachModification(), true, SkyowalletAPI.getSkyowalletPayTaxRate());
		
		if(player.isOnline()) {
			player.getPlayer().sendMessage(PlaceholderFormatter.defaultFormat(Skyowallet.messages.message9, sender, amount, amount));
		}
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}