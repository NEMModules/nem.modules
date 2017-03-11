package org.nem.modules.mcm;

import org.apache.commons.cli.*;

import java.util.concurrent.ExecutionException;

public class Main {

	// example input:
	// $publicKey1 = 95c9b9624540bbf461eda4d526908848a8fe5479058095e92e57a2ed16c6840f
	// $publicKey2 = f76b23f89550ef41e2fe4c6016d8829f1cb8e4adab1826eb4b735a25959886ed
	// $publicKey3 = 9d4c6ea216bf2a71f58598bdc968f92fc28f4a6e4adf2fd24cbd146810170087
	// $fundingPrivateKey = <hex-string>
	// nem-modules-mcm \
	//   -n 3 -m 2 -cosigners $publicKey1 $publicKey2 $publicKey3 \
	//   -signer $fundingPrivateKey \
	//   -host 37.187.70.29 -port 7890

	public static void main(final String[] args) throws ParseException, ExecutionException, InterruptedException {
		System.out.println("> parsing options");
		final MultisigOptions multisigOptions = MultisigOptions.Parse(args);
		if (null == multisigOptions)
			return;

		ConsolePrinter.PrintOptions(multisigOptions);

		final MultisigCreatorModule module = new MultisigCreatorModule(multisigOptions, 4);

		System.out.println("> creating multisig account");
		ConsolePrinter.PrintAccount(module.multisigKeyPair());
		module.create();

		System.out.println("> done!");
	}
}