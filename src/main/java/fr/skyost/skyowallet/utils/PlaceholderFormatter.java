package fr.skyost.skyowallet.utils;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.SkyowalletBank;

/**
 * Little utility class that allows to format <b>Strings</b>.
 */

public class PlaceholderFormatter {
	
	public static final LineBreakPlaceholder LINEBREAK_HOLDER = new LineBreakPlaceholder();
	
	/**
	 * Allows you to format a <b>String</b> with <b>Placeholders</b>.
	 * 
	 * @param string The <b>String</b> to format.
	 * @param placeholders The <b>Placeholders</b> to use.
	 * 
	 * @return The formatted <b>String</b>.
	 */
	
	public static final String format(final String string, final Placeholder... placeholders) {
		String result = string;
		for(final Placeholder placeholder : placeholders) {
			result = placeholder.format(result);
		}
		return LINEBREAK_HOLDER.format(result);
	}
	
	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 * 
	 * @param string The <b>String</b> to format.
	 * @param args The default arguments.
	 * 
	 * @return The formatted <b>String</b>.
	 */
	
	public static final String defaultFormat(final String string, final Object... args) {
		String result = string;
		if(args.length > 0) {
			if(args[0] instanceof CommandSender) {
				result = new PlayerPlaceholder((CommandSender)args[0]).format(result);
			}
			else if(args[0] instanceof OfflinePlayer) {
				result = new PlayerPlaceholder((OfflinePlayer)args[0]).format(result);
			}
			else if(args[0] instanceof UUID) {
				result = new PlayerPlaceholder((UUID)args[0]).format(result);
			}
			if(args.length > 1) {
				if(args[1] instanceof Double) {
					result = new AmountPlaceholder((Double)args[1]).format(result);
				}
				if(args.length > 2) {
					if(args[2] instanceof Double) {
						result = new CurrencyNamePlaceholder((Double)args[1]).format(result);
					}
					if(args.length > 3) {
						if(args[3] instanceof SkyowalletBank) {
							result = new BankPlaceholder((SkyowalletBank)args[3]).format(result);
						}
					}
				}
			}
		}
		return LINEBREAK_HOLDER.format(result);
	}
	
	/**
	 * Represents a simple <b>Placeholder</b>.
	 */
	
	public static class Placeholder {
		
		private String placeholder;
		private String string;
		
		/**
		 * Creates a <b>Placeholder</b> instance.
		 * 
		 * @param placeholder The corresponding placeholder.
		 * @param string The <b>String</b>.
		 */
		
		public Placeholder(final String placeholder, final String string) {
			this.placeholder = placeholder;
			this.string = string;
		}
		
		/**
		 * Gets the corresponding placeholder.
		 * 
		 * @return The corresponding placeholder.
		 */
		
		public final String getPlaceholder() {
			return placeholder;
		}
		
		/**
		 * Sets the corresponding placeholder.
		 * 
		 * @param placeholder The corresponding placeholder.
		 */
		
		public final void setPlaceholder(final String placeholder) {
			this.placeholder = placeholder;
		}
		
		/**
		 * Gets the <b>String</b>.
		 * 
		 * @return The <b>String</b>.
		 */
		
		public final String getString() {
			return string;
		}
		
		/**
		 * Sets the <b>String</b>.
		 * 
		 * @param string The <b>String</b>.
		 */
		
		public final void setString(final String string) {
			this.string = string;
		}
		
		/**
		 * Formats a <b>String</b> with this placeholder.
		 * 
		 * @param string The <b>String</b> to format.
		 * 
		 * @return The formatted <b>String</b>.
		 */
		
		public final String format(final String string) {
			return string.replace(this.placeholder, this.string);
		}
		
	}
	
	/**
	 * Represents a /n/ <b>Placeholder</b>.
	 */
	
	public static class LineBreakPlaceholder extends Placeholder {
		
		public LineBreakPlaceholder() {
			super("/bank/", "/n/");
		}
		
	}
	
	/**
	 * Represents a /amount/ <b>Placeholder</b>.
	 */
	
	public static class AmountPlaceholder extends Placeholder {
		
		public AmountPlaceholder(final Double amount) {
			super("/amount/", String.valueOf(amount));
		}
		
	}
	
	/**
	 * Represents a /bank/ <b>Placeholder</b>.
	 */
	
	public static class BankPlaceholder extends Placeholder {
		
		public BankPlaceholder(final SkyowalletBank bank) {
			super("/bank/", bank.getName());
		}
		
	}
	
	/**
	 * Represents a /currency-name/ <b>Placeholder</b>.
	 */
	
	public static class CurrencyNamePlaceholder extends Placeholder {
		
		public CurrencyNamePlaceholder(final Double amount) {
			super("/currency-name/", SkyowalletAPI.getCurrencyName(amount));
		}
		
	}
	
	/**
	 * Represents a /player/ <b>Placeholder</b>.
	 */
	
	public static class PlayerPlaceholder extends Placeholder {
		
		public PlayerPlaceholder(final CommandSender sender) {
			super("/player/", sender.getName());
		}
		
		public PlayerPlaceholder(final OfflinePlayer player) {
			super("/player/", player.getName());
		}
		
		public PlayerPlaceholder(final UUID uuid) {
			super("/player/", Bukkit.getOfflinePlayer(uuid) == null ? uuid.toString() : Bukkit.getOfflinePlayer(uuid).getName());
		}
		
	}
	
}