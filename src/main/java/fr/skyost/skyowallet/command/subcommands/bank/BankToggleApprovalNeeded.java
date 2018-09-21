package fr.skyost.skyowallet.command.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

/**
 * Represents the <em>/bank toggleapprovalneeded</em> command.
 */

public class BankToggleApprovalNeeded implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"toggleapprovalneeded", "toggle-approval-needed", "tan"};
	}

	@Override
	public final boolean mustBePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.toggleapprovalneeded";
	}

	@Override
	public final int getMinArgsLength() {
		return 0;
	}

	@Override
	public final String getUsage() {
		return "[bank]";
	}

	@Override
	public boolean onCommand(final SubCommandsExecutor command, final CommandSender sender, final String[] args) {
		final Skyowallet skyowallet = command.getSkyowallet();
		SkyowalletBank bank;
		
		if(args.length > 0) {
			bank = skyowallet.getBankManager().get(args[0]);
			if(bank == null) {
				sender.sendMessage(skyowallet.getPluginMessages().messageUnexistingBank);
				return true;
			}
		}
		else {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Console : " + getUsage().replace("[", "<").replace("]", ">"));
				return true;
			}

			final SkyowalletAccountManager accountManager = skyowallet.getAccountManager();
			if(!accountManager.has((OfflinePlayer)sender)) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoAccount);
				return true;
			}
			
			final SkyowalletAccount account = accountManager.get((OfflinePlayer)sender);
			bank = account.getBank();
			if((bank == null || !account.isBankOwner()) && !sender.hasPermission("skyowallet.admin")) {
				sender.sendMessage(skyowallet.getPluginMessages().messageNoPermission);
				return true;
			}
		}

		if(bank.isApprovalRequired()) {
			final String message = PlaceholderFormatter.format(skyowallet.getPluginMessages().messageBankRequestAccepted, new PlayerPlaceholder(sender), new BankPlaceholder(bank), new Placeholder("reason", skyowallet.getPluginMessages().messageNoReason));
			for(final SkyowalletAccount playerAccount : bank.getPendingMembers()) {
				playerAccount.setBankRequest(null);
				playerAccount.setBank(bank);

				final OfflinePlayer player = Bukkit.getOfflinePlayer(playerAccount.getUUID());
				if(player != null && player.isOnline()) {
					player.getPlayer().sendMessage(message);
				}
			}
			bank.setApprovalRequired(false);
		}
		else {
			bank.setApprovalRequired(true);
		}

		sender.sendMessage(skyowallet.getPluginMessages().messageDone);
		return true;
	}

}