package fr.skyost.skyowallet.tasks;

import java.util.UUID;

import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.SkyowalletAPI;

public class SyncTask extends Thread {
	
	private final CommandSender sender;
	private final UUID uuid;
	
	public SyncTask(final CommandSender sender) {
		this(sender, null);
	}
	
	public SyncTask(final CommandSender sender, final UUID uuid) {
		this.sender = sender;
		this.uuid = uuid;
	}

	@Override
	public final void run() {
		if(uuid == null) {
			SkyowalletAPI.sync(sender);
		}
		else {
			SkyowalletAPI.sync(sender, uuid == null ? null : SkyowalletAPI.getAccount(uuid));
		}
	}

}