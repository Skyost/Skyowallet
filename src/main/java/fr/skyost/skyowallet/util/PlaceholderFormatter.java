package fr.skyost.skyowallet.util;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.bank.SkyowalletBank;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.UUID;

/**
 * Little utility class that allows to format <b>Strings</b>.
 */

public class PlaceholderFormatter {

	/**
	 * The line break placeholder.
	 */
	
	public static final LineBreakPlaceholder LINEBREAK_HOLDER = new LineBreakPlaceholder();
	
	/**
	 * Allows you to format a <b>String</b> with <b>Placeholders</b>.
	 * 
	 * @param string The <b>String</b> to format.
	 * @param placeholders The <b>Placeholders</b> to use.
	 * 
	 * @return The formatted <b>String</b>.
	 */
	
	public static String format(final String string, final Placeholder... placeholders) {
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
	 * @param sender The sender.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final CommandSender sender) {
		return format(string, new PlayerPlaceholder(sender));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param player The player.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final OfflinePlayer player) {
		return format(string, new PlayerPlaceholder(player));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param uuid The UUID.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final UUID uuid) {
		return format(string, new PlayerPlaceholder(uuid));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param sender The sender.
	 * @param amount The amount.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final CommandSender sender, final double amount) {
		return new CurrencyNamePlaceholder(amount).format(new AmountPlaceholder(amount).format(defaultFormat(string, sender)));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param player The player.
	 * @param amount The amount.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final OfflinePlayer player, final double amount) {
		return new CurrencyNamePlaceholder(amount).format(new AmountPlaceholder(amount).format(defaultFormat(string, player)));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param uuid The UUID.
	 * @param amount The amount.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final UUID uuid, final double amount) {
		return new CurrencyNamePlaceholder(amount).format(new AmountPlaceholder(amount).format(defaultFormat(string, uuid)));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param sender The sender.
	 * @param amount The amount.
	 * @param bank The bank.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final CommandSender sender, final double amount, final SkyowalletBank bank) {
		return new BankPlaceholder(bank).format(defaultFormat(string, sender, amount));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param player The player.
	 * @param amount The amount.
	 * @param bank The bank.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final OfflinePlayer player, final double amount, final SkyowalletBank bank) {
		return new BankPlaceholder(bank).format(defaultFormat(string, player, amount));
	}

	/**
	 * Allows you to format a <b>String</b> with the default <b>Placeholders</b>.
	 *
	 * @param string The <b>String</b> to format.
	 * @param uuid The UUID.
	 * @param amount The amount.
	 * @param bank The bank.
	 *
	 * @return The formatted <b>String</b>.
	 */

	public static String defaultFormat(final String string, final UUID uuid, final double amount, final SkyowalletBank bank) {
		return new BankPlaceholder(bank).format(defaultFormat(string, uuid, amount));
	}
	
	/**
	 * Represents a simple <b>Placeholder</b>.
	 */
	
	public static class Placeholder {

		/**
		 * The placeholder name.
		 */

		private String placeholder;

		/**
		 * The <b>String</b>.
		 */

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
		 * Returns the corresponding placeholder.
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
		 * Returns the <b>String</b>.
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
			return string.replace("/" + this.placeholder + "/", this.string);
		}
		
	}
	
	/**
	 * Represents a /n/ <b>Placeholder</b>.
	 */
	
	public static class LineBreakPlaceholder extends Placeholder {
		
		public LineBreakPlaceholder() {
			super("n", "\n");
		}
		
	}
	
	/**
	 * Represents a /amount/ <b>Placeholder</b>.
	 */
	
	public static class AmountPlaceholder extends Placeholder {

		/**
		 * Creates a new amount placeholder instance.
		 *
		 * @param amount The amount.
		 */
		
		public AmountPlaceholder(final Double amount) {
			super("amount", String.valueOf(amount));
		}
		
	}
	
	/**
	 * Represents a /bank/ <b>Placeholder</b>.
	 */
	
	public static class BankPlaceholder extends Placeholder {
		
		public BankPlaceholder(final SkyowalletBank bank) {
			super("bank", bank.getName());
		}
		
	}
	
	/**
	 * Represents a /currency-name/ <b>Placeholder</b>.
	 */
	
	public static class CurrencyNamePlaceholder extends Placeholder {
		
		public CurrencyNamePlaceholder(final Double amount) {
			super("currency-name", Skyowallet.getInstance().getPluginConfig().getCurrencyName(amount));
		}
		
	}
	
	/**
	 * Represents a /player/ <b>Placeholder</b>.
	 */
	
	public static class PlayerPlaceholder extends Placeholder {
		
		public PlayerPlaceholder(final CommandSender sender) {
			super("player", sender.getName());
		}
		
		public PlayerPlaceholder(final OfflinePlayer player) {
			super("player", Util.getName(player));
		}
		
		public PlayerPlaceholder(final UUID uuid) {
			super("player", Bukkit.getOfflinePlayer(uuid) == null ? uuid.toString() : Util.getName(Bukkit.getOfflinePlayer(uuid)));
		}
		
	}
	
}