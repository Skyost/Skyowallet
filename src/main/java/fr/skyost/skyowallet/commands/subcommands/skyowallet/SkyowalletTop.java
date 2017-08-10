package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.utils.Utils;

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
	public boolean onCommand(final CommandSender sender, final String[] args) {
		Integer number = 10;
		if(args.length > 0) {
			number = Utils.integerTryParse(args[0]);
			if(number == null || number < 1) {
				sender.sendMessage(Skyowallet.messages.message45);
				return true;
			}
		}
		final List<SkyowalletAccount> accounts = Arrays.asList(SkyowalletAPI.getAccounts());
		Collections.sort(accounts, new Comparator<SkyowalletAccount>() {
			
		    @Override
		    public final int compare(final SkyowalletAccount account1, final SkyowalletAccount account2) {
		        return Double.compare(account1.getWallet() + account1.getBankBalance(), account2.getWallet() + account2.getBankBalance());
		    }
		    
		});
		final int size = Math.min(number, accounts.size());
		for(int i = 0; i != size; i++) {
			final SkyowalletAccount account = accounts.get(i);
			final double amount = account.getWallet() + account.getBankBalance();
			sender.sendMessage(PlaceholderFormatter.defaultFormat(Skyowallet.messages.message46, account.getUUID(), amount, amount));
		}
		sender.sendMessage(Utils.SEPARATOR);
		sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message47, new Placeholder("/players/", String.valueOf(size))));
		return true;
	}

}