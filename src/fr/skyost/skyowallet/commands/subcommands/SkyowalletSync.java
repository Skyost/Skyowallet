package fr.skyost.skyowallet.commands.subcommands;

import org.bukkit.command.CommandSender;
import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;

public class SkyowalletSync implements CommandInterface {

	@Override
	public final String[] names() {
		return new String[]{"sync", "reload"};
	}

	@Override
	public final boolean forcePlayer() {
		return false;
	}

	@Override
	public final String getPermission() {
		return "skyowallet.sync";
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
		new Thread() {
			
			@Override
			public final void run() {
				SkyowalletAPI.sync(sender);
			}
			
		}.start();
		return true;
	}

}