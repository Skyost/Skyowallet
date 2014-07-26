package fr.skyost.skyowallet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import fr.skyost.skyowallet.SkyowalletAPI;

public class Utils {
	
	public static final OfflinePlayer getPlayerByArgument(final String arg) {
		final UUID uuid = uuidTryParse(arg);
		return uuid == null ? Bukkit.getOfflinePlayer(arg) : Bukkit.getOfflinePlayer(uuid);
	}
	
	public static final String getFileContent(final File file, final String lineSeparator) throws IOException {
		final BufferedReader reader = new BufferedReader(new FileReader(file));
		final StringBuilder builder = new StringBuilder();
		try {
			String line = reader.readLine();
			while(line != null) {
				builder.append(line);
				if(lineSeparator != null) {
					builder.append(lineSeparator);
				}
				line = reader.readLine();
			}
		}
		finally {
			reader.close();
		}
		return builder.toString();
	}
	
	public static final void writeToFile(final File file, final String content) throws IOException {
		if(!file.exists()) {
			file.createNewFile();
		}
		final FileWriter fileWriter = new FileWriter(file, false);
		final PrintWriter printWriter = new PrintWriter(fileWriter, true);
		printWriter.println(content);
		printWriter.close();
		fileWriter.close();
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
