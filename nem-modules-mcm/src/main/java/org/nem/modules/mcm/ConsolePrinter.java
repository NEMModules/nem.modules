package org.nem.modules.mcm;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.*;

public class ConsolePrinter {

	public static void PrintOptions(final MultisigOptions options) {
		System.out.println(String.format("signer: %s", options.signer()));
		System.out.println(String.format("server: %s", options.endpoint()));
		System.out.println(String.format("m %d of n %d", options.m(), options.n()));
		System.out.println("cosigners:");
		for (final Account account : options.cosigners())
			System.out.println(String.format(" - %s", account));
	}

	public static void PrintAccount(final KeyPair keyPair) {
		System.out.println(String.format("address: %s", new Account(keyPair).getAddress()));
		System.out.println(String.format("pub key: %s", keyPair.getPublicKey()));
		System.out.println(String.format("pri key: %s", keyPair.getPrivateKey()));
	}

	public static void PrintTransaction(final Transaction transaction) {
		System.out.println(String.format("address  %s", transaction.getSigner()));
		System.out.println(String.format("sig      %s", transaction.getSignature()));
		System.out.println(String.format("fee      %s", transaction.getFee()));
		System.out.println(String.format("type     %s", transaction.getType()));
		System.out.println(String.format("deadline %s", transaction.getDeadline()));
	}
}
