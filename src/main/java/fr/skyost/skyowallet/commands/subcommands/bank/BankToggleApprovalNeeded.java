package fr.skyost.skyowallet.commands.subcommands.bank;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletAccount;
import fr.skyost.skyowallet.SkyowalletBank;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;

public class BankToggleApprovalNeeded implements CommandInterface {
	
	@Override
	public final String[] getNames() {
		return new String[]{"toggleapprovalneeded", "toggle-approval-needed", "tan"};
	}

	@Override
	public final boolean mustBePlayer() {
		return true;
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
		return null;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		if(!SkyowalletAPI.hasAccount((OfflinePlayer)sender)) {
			sender.sendMessage(Skyowallet.messages.message33);
			return true;
		}
		
		final SkyowalletAccount account = SkyowalletAPI.getAccount((OfflinePlayer)sender);
		final SkyowalletBank bank = account.getBank();
		if((bank == null || !account.isBankOwner()) && !sender.hasPermission("skyowallet.admin")) {
			sender.sendMessage(Skyowallet.messages.message1);
			return true;
		}
		
		if(bank.isApprovalRequired()) {
			final String message = Skyowallet.messages.message39.replace("/player/", sender.getName()).replace("/bank/", bank.getName()).replace("/reason/", Skyowallet.messages.message41);
			for(final SkyowalletAccount playerAccount : bank.getPendingMembers()) {
				playerAccount.setBankRequest(null, false);
				playerAccount.setBank(bank, false);
				
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
		sender.sendMessage(Skyowallet.messages.message10);
		return true;
	}

}