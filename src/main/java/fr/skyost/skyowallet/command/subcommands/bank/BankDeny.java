package fr.skyost.skyowallet.command.subcommands.bank;

import com.google.common.base.Joiner;
import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.command.SubCommandsExecutor;
import fr.skyost.skyowallet.command.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;
import fr.skyost.skyowallet.economy.account.SkyowalletAccountManager;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import fr.skyost.skyowallet.util.PlaceholderFormatter;
import fr.skyost.skyowallet.util.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.Placeholder;
import fr.skyost.skyowallet.util.PlaceholderFormatter.PlayerPlaceholder;
import fr.skyost.skyowallet.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Represents the <em>/bank deny</em> command.
 */

public class BankDeny implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"deny"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.deny";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[player | uuid] [reason]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		if(sender instanceof Player && !BankApprove.playerTests(skyowallet, (Player)sender, args)) {
			return true;
		}
		else if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replaceFirst("\\[", "<").replaceFirst("]", ">"));
			return true;
		}

		final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();

		final OfflinePlayer player = Util.getPlayerByArgument(args[0]);
		if(player == null || !accountManager.has(player)) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerNoAccount);
			return true;
		}

		final SkyowalletAccount playerAccount = accountManager.get(player);
		final SkyowalletBank bank = playerAccount.getBankRequest();
		if(bank == null) {
			sender.sendMessage(skyowallet.getPluginMessages().messagePlayerBankNotRequested);
			return true;
		}
		
		playerAccount.setBankRequest(null);
		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		
		if(player.isOnline()) {
			String reason = skyowallet.getPluginMessages().messageNoReason;
			if(args.length > 1) {
				reason = Joiner.on(' ').join(Arrays.copyOfRange(args, 1, args.length));
			}
			player.getPlayer().sendMessage(PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestDenied, new PlayerPlaceholder(sender), new BankPlaceholder(bank), new Placeholder("reason", reason)));
		}
		return true;
	}

}