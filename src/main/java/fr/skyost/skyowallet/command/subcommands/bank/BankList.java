package fr.skyost.skyowallet.command.subcommands.bank;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.EconomyOperations;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.AmountPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.CurrencyNamePlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Represents the <em>/bank list</em> command.
 */

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
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		final EconomyOperations economyOperations = skyowallet.getEconomyOperations();

		final TreeMap<Double, Object[]> banksData = new TreeMap<>();
		for(final SkyowalletBank bank : skyowallet.getBankManager().list()) {
			final Collection<Double> balances = bank.getMembers().values();
			double bankBalance = 0.0;
			for(final double balance : balances) {
				bankBalance += balance;
			}
			banksData.put(economyOperations.round(bankBalance), new Object[]{bank, balances.size()});
		}
		for(final Entry<Double, Object[]> entry : banksData.entrySet()) {
			final Object[] data = entry.getValue();
			final double amount = entry.getKey();
			sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRanking, new AmountPlaceholder(amount), new CurrencyNamePlaceholder(amount), new BankPlaceholder((SkyowalletBank)data[0]), new Placeholder("accounts", data[1].toString())));
		}
		sender.sendMessage(Util.SEPARATOR);
		sender.sendMessage(skyowallet.getPluginMessages().messageBankCount.replace("/banks/", String.valueOf(banksData.size())));
		return true;
	}

}