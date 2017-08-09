package fr.skyost.skyowallet.commands.subcommands.skyowallet;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.commands.SubCommandsExecutor.CommandInterface;
import fr.skyost.skyowallet.tasks.SyncTask;
import fr.skyost.skyowallet.utils.Utils;

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
		return "[player | uuid]";
	}

	@Override
	public boolean onCommand(final CommandSender sender, final String[] args) {
		UUID uuid = null;
		if(args.length > 0) {
			final OfflinePlayer player = Utils.getPlayerByArgument(args[0]);
			if(player != null) {
				uuid = player.getUniqueId();
			}
		}
		new SyncTask(sender, uuid).start();
		return true;
	}

}