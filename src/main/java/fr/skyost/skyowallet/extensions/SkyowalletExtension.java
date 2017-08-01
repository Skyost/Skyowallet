package fr.skyost.skyowallet.extensions;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import fr.skyost.skyowallet.SkyowalletAPI;
import fr.skyost.skyowallet.utils.Skyoconfig;

public abstract class SkyowalletExtension implements Listener {
	
	private boolean loaded = false;
	private final JavaPlugin plugin;
	
	protected SkyowalletExtension(final JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Gets the plugin this extension belongs to.
	 * 
	 * @return The plugin this extension belongs to.
	 */
	
	public final JavaPlugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Loads the extension (register its events and load the configuration).
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
	 * Unload this extension (unregister events and commands making this extension useless).
	 * 
	 * @throws InvalidConfigurationException If the config cannot be saved.
	 */
	
	public void unload() throws InvalidConfigurationException {
		if(!loaded) {
			return;
		}
		HandlerList.unregisterAll(this);
		for(final String command : this.getCommands().keySet()) {
			plugin.getCommand(command).setExecutor(null);
		}
		final Skyoconfig config = getConfiguration();
		config.save();
		loaded = false;
	}
	
	/**
	 * Checks if this extensions loaded (not enabled but initialized and loaded).
	 * 
	 * @return Whether this extension is loaded.
	 */
	
	public boolean isLoaded() {
		return loaded;
	}
	
	/**
	 * Gets the extension's name.
	 * 
	 * @return The extension's name.
	 */
	
	public abstract String getName();
	
	/**
	 * Gets the extension's permissions.
	 * <br><b>Key :</b> Permission's name.
	 * <br><b>Value :</b> Default value.
	 * 
	 * @return The extension's permissions.
	 */
	
	public Map<String, PermissionDefault> getPermissions() {
		return new HashMap<String, PermissionDefault>();
	}
	
	/**
	 * Gets the extension's commands.
	 * <br><b>Key :</b> Command's name.
	 * <br><b>Value :</b> The executor.
	 * 
	 * @return The extension's commands.
	 */
	
	public Map<String, CommandExecutor> getCommands() {
		return new HashMap<String, CommandExecutor>();
	}
	
	/**
	 * Gets the extension's YAML configuration.
	 * 
	 * @return The YAML configuration.
	 */
	
	public abstract SkyowalletExtensionConfig getConfiguration();
	
	/**
	 * Gets the extension's configuration file.
	 * 
	 * @return The configuration file.
	 */
	
	public final File getConfigurationFile() {
		return new File(SkyowalletAPI.getExtensionsDirectory(), getFileName());
	}
	
	/**
	 * Gets the extension's configuration file name.
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
	 */
	
	public boolean isEnabled() {
		final SkyowalletExtensionConfig config = getConfiguration();
		return config == null ? false : config.enable;
	}
	
	/**
	 * Represents an extension configuration.
	 */
	
	public class SkyowalletExtensionConfig extends Skyoconfig {
		
		@ConfigOptions(name = "enable")
		public boolean enable = false;

		public SkyowalletExtensionConfig() {
			super(getConfigurationFile(), Arrays.asList(getName() + " Configuration"));
		}
		
	}
	
}