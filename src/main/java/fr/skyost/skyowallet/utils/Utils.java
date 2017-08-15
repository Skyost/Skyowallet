package fr.skyost.skyowallet.utils;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Charsets;

import fr.skyost.skyowallet.SkyowalletAPI;

public class Utils {
	
	public static final String SEPARATOR = ChatColor.GRAY + "-----------------------------------------------------";
	
	public static final OfflinePlayer getPlayerByArgument(final String arg) {
		final UUID uuid = uuidTryParse(arg);
		final OfflinePlayer player = uuid == null ? Bukkit.getOfflinePlayer(arg) : Bukkit.getOfflinePlayer(uuid);
		if(player == null && uuid == null) {
			return Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + arg).getBytes(Charsets.UTF_8)));
		}
		return player;
	}
	
	public static final String getName(final OfflinePlayer player) {
		return player.hasPlayedBefore() ? player.getName() : player.getUniqueId().toString();
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
		catch(final Exception ex) {}
		return null;
	}
	
	public static final Integer integerTryParse(final String string) {
		try {
			return Integer.parseInt(string);
		}
		catch(final Exception ex) {}
		return null;
	}
	
	public static final Long longTryParse(final String string) {
		try {
			return Long.parseLong(string);
		}
		catch(final Exception ex) {}
		return null;
	}
	
	public static final Boolean booleanTryParse(final String string) {
		try {
			return Boolean.parseBoolean(string);
		}
		catch(final Exception ex) {}
		return null;
	}
	
	public static final UUID uuidTryParse(final String string) {
		try {
			return UUID.fromString(string);
		}
		catch(final Exception ex) {}
		return null;
	}
	
	public static final String uuidAddDashes(final String uuid) {
		return uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
	}
	
}