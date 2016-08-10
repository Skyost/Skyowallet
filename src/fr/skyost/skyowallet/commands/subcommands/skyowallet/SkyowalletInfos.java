package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
		final Plugin plugin = Bukkit.getPluginManager().getPlugin("Skyowallet");
		sender.sendMessage(ChatColor.GOLD + "Skyowallet v" + plugin.getDescription().getVersion());
		final SkyowalletAccount[] accounts = SkyowalletAPI.getAccounts();
		sender.sendMessage(Skyowallet.messages.message5.replace("/total-accounts/", String.valueOf(accounts.length)));
		double totalWallet = 0.0;
		double bestWallet = 0.0;
		for(final SkyowalletAccount account : accounts) {
			final double wallet = account.getWallet();
			totalWallet += wallet;
			if(bestWallet < wallet) {
				bestWallet = wallet;
			}
		}
		sender.sendMessage(Skyowallet.messages.message6.replace("/amount/", String.valueOf(totalWallet)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(totalWallet)));
		sender.sendMessage(Skyowallet.messages.message7.replace("/amount/", String.valueOf(bestWallet)).replace("/currency-name/", SkyowalletAPI.getCurrencyName(bestWallet)));
		return true;
	}

}