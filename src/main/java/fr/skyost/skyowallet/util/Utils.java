package fr.skyost.skyowallet.util;

import com.google.common.base.Charsets;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.UUID;

import fr.skyost.skyowallet.Skyowallet;

/**
 * Utilities methods.
 */

public class Utils {

	/**
	 * The default chat separator.
	 */
	
	public static final String SEPARATOR = ChatColor.GRAY + "-----------------------------------------------------";

	/**
	 * Returns the player provided by an argument.
	 *
	 * @param arg The argument.
	 *
	 * @return The player.
	 */
	
	public static OfflinePlayer getPlayerByArgument(final String arg) {
		final UUID uuid = uuidTryParse(arg);
		final OfflinePlayer player = uuid == null ? Bukkit.getOfflinePlayer(arg) : Bukkit.getOfflinePlayer(uuid);
		if(player == null && uuid == null) {
			return Bukkit.getOfflinePlayer(UUID.nameUUIDFromBytes(("OfflinePlayer:" + arg).getBytes(Charsets.UTF_8)));
		}
		return player;
	}

	/**
	 * Returns the name of the specified player.
	 *
	 * @param player The player.
	 *
	 * @return The player's name.
	 */
	
	public static String getName(final OfflinePlayer player) {
		return player.getName() == null ? player.getUniqueId().toString() : player.getName();
	}

	/**
	 * Returns whether the provided name is a valid file name.
	 *
	 * @param name The name.
	 *
	 * @return Whether the provided name is a valid file name.
	 */
	
	public static boolean isValidFileName(final String name) {
		final File file = new File(Skyowallet.getInstance().getDataFolder(), name);
		try {
			if(file.createNewFile()) {
				file.delete();
				return true;
			}
		}
		catch(final Exception ignored) {}
		return false;
	}

	/**
	 * Try to parse a double.
	 *
	 * @param string The String.
	 *
	 * @return The result if the parse is a success, null otherwise.
	 */
	
	public static Double doubleTryParse(final String string) {
		try {
			return Double.parseDouble(string);
		}
		catch(final Exception ex) {}
		return null;
	}

	/**
	 * Try to parse an integer.
	 *
	 * @param string The String.
	 *
	 * @return The result if the parse is a success, null otherwise.
	 */
	
	public static Integer integerTryParse(final String string) {
		try {
			return Integer.parseInt(string);
		}
		catch(final Exception ex) {}
		return null;
	}

	/**
	 * Try to parse an UUID.
	 *
	 * @param string The String.
	 *
	 * @return The result if the parse is a success, null otherwise.
	 */
	
	public static UUID uuidTryParse(final String string) {
		try {
			return UUID.fromString(string);
		}
		catch(final Exception ex) {}
		return null;
	}

	/**
	 * Adds missing dashes to an UUID.
	 *
	 * @param uuid The UUID.
	 *
	 * @return The UUID with dashes.
	 */
	
	public static String uuidAddDashes(final String uuid) {
		return uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
	}
	
}