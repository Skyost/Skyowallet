package fr.skyost.skyowallet.commands.subcommands.bank;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;

public class BankList implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"list"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.list";
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
		final TreeMap<Double, Object[]> banksData = new TreeMap<Double, Object[]>();
		for(final SkyowalletBank bank : SkyowalletAPI.getBanks()) {
			final Collection<Double> balances = bank.getMembers().values();
			double bankBalance = 0.0;
			for(final double balance : balances) {
				bankBalance += balance;
			}
			banksData.put(bankBalance, new Object[]{bank.getName(), balances.size()});
		}
		for(final Entry<Double, Object[]> entry : banksData.entrySet()) {
			final Object[] data = entry.getValue();
			sender.sendMessage(Skyowallet.messages.message27.replace("/bank/", data[0].toString()).replace("/accounts/", data[1].toString()).replace("/amount/", String.valueOf(entry.getKey())));
		}
		sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------------");
		sender.sendMessage(Skyowallet.messages.message32.replace("/banks/", String.valueOf(banksData.size())));
		return true;
	}

}
