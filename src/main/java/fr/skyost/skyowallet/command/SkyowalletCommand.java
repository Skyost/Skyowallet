package fr.skyost.skyowallet.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.util.PlaceholderFormatter;

/**
 * Represents the <em>/skyowallet</em> command.
 */

public class SkyowalletCommand extends SubCommandsExecutor {

	/**
	 * Creates a new skyowallet command instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletCommand(final Skyowallet skyowallet) {
		super(skyowallet, "skyowallet");
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label, String[] args) {
		if(args.length <= 0) {
			if(!(sender instanceof Player)) {
				sendUsage(sender);
				return true;
			}

			final Skyowallet skyowallet = getSkyowallet();
			final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

			if(!accountManager.has((Player)sender)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}

			final double wallet = accountManager.get((Player)sender).getWallet().getAmount();
			sender.sendMessage(PlaceholderFormatter.defaultFormat(skyowallet.getPluginMessages().messageWelcome, sender, wallet));
			return true;
		}
		return super.onCommand(sender, command, label, args);
	}

}