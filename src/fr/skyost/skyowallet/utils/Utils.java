package fr.skyost.skyowallet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.UUID;

public class Utils {
	
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
	
	public static final long getCurrentTimeInMillis() {
		return Calendar.getInstance().getTimeInMillis();
	}
	
	public static final Double doubleTryParse(final String string) {
		try {
			return Double.parseDouble(string);
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
	
}
