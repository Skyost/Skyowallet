package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;

public class SkyowalletInfos implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"infos"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.infos";
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
		final Skyowallet plugin = SkyowalletAPI.getPlugin();
		sender.sendMessage(ChatColor.GOLD + plugin.getName() + " v" + plugin.getDescription().getVersion());
		
		final SkyowalletAccount[] accounts = SkyowalletAPI.getAccounts();
		sender.sendMessage(Skyowallet.messages.message5.replace("/total-accounts/", String.valueOf(accounts.length)));
		if(accounts.length == 0) {
			return true;
		}
		
		double totalMoney = 0d;
		SkyowalletAccount bestAccount = null;
		for(final SkyowalletAccount account : accounts) {
			final double amount = account.getWallet() + account.getBankBalance();
			totalMoney += amount;
			if(bestAccount == null || bestAccount.getWallet() + bestAccount.getBankBalance() < amount) {
				bestAccount = account;
			}
		}
		totalMoney = SkyowalletAPI.round(totalMoney);
		
		final double bestAccountAmount = bestAccount.getWallet() + bestAccount.getBankBalance();
		final OfflinePlayer player = Bukkit.getOfflinePlayer(bestAccount.getUUID());
		sender.sendMessage(Skyowallet.messages.message6.replace("/amount/", String.valueOf(totalMoney)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(totalMoney)));
		sender.sendMessage(Skyowallet.messages.message7.replace("/amount/", String.valueOf(bestAccountAmount)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(bestAccountAmount)).replace("/player/", player == null ? bestAccount.getUUID().toString() : player.getName()));
		return true;
	}

}