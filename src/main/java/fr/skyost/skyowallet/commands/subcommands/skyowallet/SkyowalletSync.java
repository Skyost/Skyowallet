package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.tasks.SyncTask;

public class SkyowalletSync implements CommandInterface {

	@Override
	public final String[] getNames() {
		return new String[]{"sync", "reload"};
	}

	@Override
	public final boolean mustBePlayer() {
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
		new SyncTask(false, sender).start();
		return true;
	}

}