package fr.skyost.skyowallet.utils;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Charsets;

import fr.skyost.skyowallet.SkyowalletAPI;

public class Utils {
	
	@SuppressWarnings("deprecation")
	public static final OfflinePlayer getPlayerByArgument(final String arg) {
		final UUID uuid = uuidTryParse(arg);
		final OfflinePlayer player = uuid == null ? Bukkit.getOfflinePlayer(arg) : Bukkit.getOfflinePlayer(uuid);
		if(player == null && uuid == null) {
			return Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + arg).getBytes(Charsets.UTF_8)));
		}
		return player;
	}
	
	public static final boolean isValidFileName(final String name) {
		final File file = new File(SkyowalletAPI.getPlugin().getDataFolder(), name);
		try {
			if(file.createNewFile()) {
				file.delete();
				return true;
			}
		}
		catch(final Exception ex) {}
		return false;
	}
	
	public static final Double doubleTryParse(final String string) {
		try {
			return Double.parseDouble(string);
		}
		catch(final NumberFormatException ex) {}
		return null;
	}
	
	public static final Long longTryParse(final String string) {
		try {
			return Long.parseLong(string);
		}
		catch(final NumberFormatException ex) {}
		return null;
	}
	
	public static final UUID uuidTryParse(final String string) {
		try {
			return UUID.fromString(string);
		}
		catch(final IllegalArgumentException ex){}
		return null;
	}
	
	public static final String uuidAddDashes(final String uuid) {
		return uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
	}
	
}