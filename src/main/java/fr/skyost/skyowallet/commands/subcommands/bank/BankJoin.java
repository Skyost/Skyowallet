package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.utils.PlaceholderFormatter;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.BankPlaceholder;
import fr.skyost.skyowallet.utils.PlaceholderFormatter.PlayerPlaceholder;

public class BankJoin implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"join"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.bank.join";
	}

	@Override
	public final int getMinArgsLength() {
		return 1;
	}

	@Override
	public final String getUsage() {
		return "<bank>";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		if(account == null) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		if(account.getBank() != null) {
			sender.sendMessage(Skyowallet.messages.message24);
			return true;
		}
		final SkyowalletBank bank = SkyowalletAPI.getBank(args[0]);
		if(bank == null) {
			sender.sendMessage(Skyowallet.messages.message19);
			return true;
		}
		if(bank.isApprovalRequired()) {
			account.setBank(bank);
			sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message25, new BankPlaceholder(bank)));
		}
		else {
			account.setBankRequest(bank);
			sender.sendMessage(PlaceholderFormatter.format(Skyowallet.messages.message34, new BankPlaceholder(bank)));
			
			final String message = PlaceholderFormatter.format(Skyowallet.messages.message36, new PlayerPlaceholder(sender));
			for(final SkyowalletAccount owner : bank.getOwners()) {
				final OfflinePlayer player = Bukkit.getOfflinePlayer(owner.getUUID());
				if(player != null && player.isOnline()) {
					player.getPlayer().sendMessage(message);
				}
			}
		}
		return true;
	}

}