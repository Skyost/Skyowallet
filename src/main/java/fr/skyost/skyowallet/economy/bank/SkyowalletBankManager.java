package fr.skyost.skyowallet.economy.bank;

import java.util.HashMap;
import java.util.Map;

import fr.skyost.skyowallet.economy.SkyowalletManager;
import fr.skyost.skyowallet.economy.account.SkyowalletAccount;

/**
 * This class allows to manage banks.
 */

public class SkyowalletBankManager extends SkyowalletManager<SkyowalletBank> {

	@Override
	public Map<SkyowalletAccount, Double> remove(final SkyowalletBank bank) {
		return remove(bank == null ? null : bank.getIdentifier());
	}

	@Override
	public Map<SkyowalletAccount, Double> remove(final String identifier) {
		final SkyowalletBank result = (SkyowalletBank)super.remove(identifier);
		if(result == null) {
			return null;
		}

		final HashMap<SkyowalletAccount, Double> members = result.getMembers();
		for(final SkyowalletAccount account : members.keySet()) {
			account.setBank(null);
		}
		for(final SkyowalletAccount account : result.getPendingMembers()) {
			account.setBankRequest(null);
			members.put(account, -1d);
		}
		return members;
	}

}