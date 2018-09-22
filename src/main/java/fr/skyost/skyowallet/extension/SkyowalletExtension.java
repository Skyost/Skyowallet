package fr.skyost.skyowallet.extension;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.util.Skyoconfig;

/**
 * Represents a Skyowallet extension.
 */

public abstract class SkyowalletExtension implements Listener {

	/**
	 * Whether this extension has been loaded.
	 */
	
	private boolean loaded = false;

	/**
	 * The Skyowallet instance.
	 */

	private Skyowallet skyowallet;

	/**
	 * The parent plugin.
	 */

	private JavaPlugin plugin;

	/**
	 * 	The extension description.
	 */

	private String description;

	/**
	 * Creates a new Skyowallet extension instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 * @param plugin The parent plugin.
	 * @param description The extension description.
	 */
	
	protected SkyowalletExtension(final Skyowallet skyowallet, final JavaPlugin plugin, final String description) {
		this.skyowallet = skyowallet;
		this.plugin = plugin;
		this.description = description;
	}

	/**
	 * Returns the Skyowallet instance.
	 *
	 * @return The Skyowallet instance.
	 */

	public Skyowallet getSkyowallet() {
		return skyowallet;
	}

	/**
	 * Sets the Skyowallet instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public void setSkyowallet(final Skyowallet skyowallet) {
		this.skyowallet = skyowallet;
	}

	/**
	 * Returns the plugin this extension belongs to.
	 * 
	 * @return The plugin this extension belongs to.
	 */
	
	public JavaPlugin getPlugin() {
		return plugin;
	}

	/**
	 * Sets the plugin to which this extension belongs to.
	 *
	 * @param plugin The plugin to which this extension belongs to.
	 */

	public void setPlugin(final JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Returns the extension's description.
	 * 
	 * @return The description of this extension.
	 */
	
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the extension's description.
	 *
	 * @param description The new extension's description.
	 */

	public void setDescription(final String description) {
		this.description = description;
	}
	
	/**
	 * Loads the extension (register its event and load the configuration).
	 * 
	 * @throws InvalidConfigurationException If there is a problem in the extension's configuration.
	 */
	
	public void load() throws InvalidConfigurationException {
		if(loaded) {
			return;
		}
		Bukkit.getPluginManager().registerEvents(this, plugin);
		getConfiguration().load();
		loaded = true;
	}
	
	/**
	 * Unload this extension (unregister event and command making this extension useless).
	 * 
	 * @throws InvalidConfigurationException If the config cannot be saved.
	 */
	
	public void unload() throws InvalidConfigurationException {
		if(!loaded) {
			return;
		}
		HandlerList.unregisterAll(this);
		for(final String command : getCommands().keySet()) {
			plugin.getCommand(command).setExecutor(null);
		}
		final Skyoconfig config = getConfiguration();
		config.save();
		loaded = false;
	}
	
	/**
	 * Checks if this extension loaded (not enabled but initialized and loaded).
	 * 
	 * @return Whether this extension is loaded.
	 */
	
	public boolean isLoaded() {
		return loaded;
	}
	
	/**
	 * Returns the extension's name.
	 * 
	 * @return The extension's name.
	 */
	
	public String getName() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Returns the extension's permissions.
	 * <br><b>Key :</b> Permission's name.
	 * <br><b>Value :</b> Default value.
	 * 
	 * @return The extension's permissions.
	 */
	
	public Map<String, PermissionDefault> getPermissions() {
		return new HashMap<>();
	}
	
	/**
	 * Returns the extension's command.
	 * <br><b>Key :</b> Command's name.
	 * <br><b>Value :</b> The executor.
	 * 
	 * @return The extension's command.
	 */
	
	public Map<String, CommandExecutor> getCommands() {
		return new HashMap<>();
	}
	
	/**
	 * Returns the extension's YAML configuration.
	 * 
	 * @return The YAML configuration.
	 */
	
	public abstract SkyowalletExtensionConfig getConfiguration();
	
	/**
	 * Returns the extension's configuration file.
	 * 
	 * @return The configuration file.
	 */
	
	public final File getConfigurationFile() {
		return new File(skyowallet.getPluginConfig().getExtensionsDirectory(), getFileName());
	}
	
	/**
	 * Returns the extension's configuration file name.
	 * 
	 * @return The file name.
	 */
	
	public String getFileName() {
		final StringBuilder builder = new StringBuilder();
		final String name = getName();
		for(int i = 0; i != name.length(); i++) {
			final char c = name.charAt(i);
			if(i != 0 && Character.isUpperCase(c)) {
				builder.append("-");
			}
			builder.append(Character.toLowerCase(c));
		}
		builder.append(".yml");
		return builder.toString();
	}
	
	/**
	 * Checks if this extension is enabled (only checks via the configuration).
	 * <br>If the configuration returned by <i>getConfiguration()</i> is null, this will return <b>false</b>.
	 *
	 * @return Whether this extension is enabled.
	 */
	
	public boolean isEnabled() {
		final SkyowalletExtensionConfig config = getConfiguration();
		return config != null && config.enable;
	}
	
	/**
	 * Represents an extension configuration.
	 */
	
	public class SkyowalletExtensionConfig extends Skyoconfig {
		
		@ConfigOptions(name = "enable")
		public boolean enable = false;

		/**
		 * Creates a new Skyowallet extension configuration.
		 */

		public SkyowalletExtensionConfig() {
			super(getConfigurationFile(), Arrays.asList(getName() + " Configuration", "", description));
		}
		
	}
	
}