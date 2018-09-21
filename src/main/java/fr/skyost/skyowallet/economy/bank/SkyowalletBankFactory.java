package fr.skyost.skyowallet.economy.bank;

import fr.skyost.skyowallet.Skyowallet;
import fr.skyost.skyowallet.economy.SkyowalletFactory;

/**
 * Allows to create banks.
 */

public class SkyowalletBankFactory extends SkyowalletFactory<SkyowalletBank, String> {

	/**
	 * Creates a new Skyowallet bank factory instance.
	 *
	 * @param skyowallet The Skyowallet instance.
	 */

	public SkyowalletBankFactory(final Skyowallet skyowallet) {
		super(skyowallet);
	}

	/**
	 * Creates a new bank.
	 *
	 * @param name The bank.
	 * @param approvalRequired Whether an approval is required.
	 * @param isDeleted Whether this bank will be deleted at the next synchronization.
	 * @param lastModificationTime The last modification time.
	 *
	 * @return The bank.
	 */

	public final SkyowalletBank create(final String name, final boolean approvalRequired, final boolean isDeleted, final long lastModificationTime) {
		return new SkyowalletBank(getSkyowallet(), name, approvalRequired, isDeleted, lastModificationTime);
	}

	@Override
	public final SkyowalletBank createFromJSON(final String json) {
		return new SkyowalletBank(getSkyowallet(), json, false);
	}

	@Override
	public final SkyowalletBank create(final String name) {
		if(name == null || name.isEmpty()) {
			return null;
		}
		return new SkyowalletBank(getSkyowallet(), name);
	}

}