package fr.skyost.skyowallet.command.subcommands.skyowallet;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the <em>/skyowallet top</em> command.
 */

public class SkyowalletTop implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"top"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.top";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[number]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();

		Integer number = 10;
		if(args.length > 0) {
			number = Util.integerTryParse(args[0]);
			if(number == null || number < 1) {
				sender.sendMessage(skyowallet.getPluginMessages().messageInvalidInteger);
				return true;
			}
		}
		final List<SkyowalletAccount> accounts = Arrays.asList(skyowallet.getAccountManager().list().toArray(new SkyowalletAccount[0]));
		accounts.sort((account1, account2) -> Double.compare(account2.getWallet().getAmount() + account2.getBankBalance().getAmount(), account1.getWallet().getAmount() + account1.getBankBalance().getAmount()));
		final int size = Math.min(number, accounts.size());
		for(int i = 0; i != size; i++) {
			final SkyowalletAccount account = accounts.get(i);
			final double amount = account.getWallet().getAmount() + account.getBankBalance().getAmount();
			sender.sendMessage(PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageAccountRanking, account.getUUID(), amount));
		}
		sender.sendMessage(Util.SEPARATOR);
		sender.sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messagePlayerCount, new Placeholder("players", String.valueOf(size))));
		return true;
	}

}