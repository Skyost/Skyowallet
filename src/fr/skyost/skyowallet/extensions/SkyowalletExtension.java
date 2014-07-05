package fr.skyost.skyowallet.extensions;

import java.util.HashMap;

import org.bukkit.permissions.PermissionDefault;

public interface SkyowalletExtension {
	
	/**
	 * Gets the extension's name.
	 * 
	 * @return The extension's name.
	 */
	
	public String name();
	
	/**
	 * Gets the extension's permissions.
	 * <br><b>Key :</b> Permission's name.
	 * <br><b>Value :</b> Default value.
	 * 
	 * @return The extension's permissions.
	 */
	
	public HashMap<String, PermissionDefault> permissions();

}
