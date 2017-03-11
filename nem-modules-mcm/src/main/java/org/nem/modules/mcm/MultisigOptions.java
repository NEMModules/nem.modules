package org.nem.modules.mcm;

import org.apache.commons.cli.*;
import org.nem.core.crypto.*;
import org.nem.core.model.*;
import org.nem.core.node.NodeEndpoint;

import java.util.*;
import java.util.stream.Collectors;

public class MultisigOptions {

	public Account signer() {
		return this.signer;
	}

	public NodeEndpoint endpoint() {
		return this.endpoint;
	}

	public int n() {
		return this.n;
	}

	public int m() {
		return this.m;
	}

	public Collection<Account> cosigners() {
		return this.cosigners;
	}

	private void validate() {
		if (this.m <= 0)
			throw new IllegalArgumentException("m must be positive");

		if (this.n < this.m)
			throw new IllegalArgumentException("m cannot be greater than n");

		if (this.n > BlockChainConstants.MAX_ALLOWED_COSIGNATORIES_PER_ACCOUNT)
			throw new IllegalArgumentException("n is too large");

		if (this.cosigners.size() != this.n)
			throw new IllegalArgumentException("exactly n cosigners must be provided");
	}

	public static MultisigOptions Parse(final String[] args) throws ParseException {
		final Options options = CreateOptions();
		if (0 == args.length) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MSGEN", options);
			return null;
		}

		final CommandLineParser parser = new PosixParser();
		final CommandLine commandLine = parser.parse(options, args);
		CheckRequiredOptions(commandLine);

		final MultisigOptions multisigOptions = Parse(commandLine);
		multisigOptions.validate();
		return multisigOptions;
	}

	private static Options CreateOptions()  {
		final Options options = new Options();
		options.addOption("signer", true, "The signer private key");
		options.addOption("host", true, "The server host");
		options.addOption("port", true, "The server port");

		options.addOption("n", true, "The total number of cosigners");
		options.addOption("m", true, "The number of required cosigners");

		final Option cosignersOption = new Option("cosigners", true, "The cosigner public keys (comma separated)");
		cosignersOption.setArgs(BlockChainConstants.MAX_ALLOWED_COSIGNATORIES_PER_ACCOUNT);
		options.addOption(cosignersOption);
		return options;
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> GetAllOptionNames() {
		return (Collection<String>)CreateOptions().getOptions().stream()
				.map(o -> ((Option)o).getOpt())
				.collect(Collectors.toList());
	}

	private static void CheckRequiredOptions(final CommandLine commandLine) {
		final Optional<String> missingOptionName = GetAllOptionNames().stream()
				.filter(optionName -> !commandLine.hasOption(optionName))
				.findFirst();
		if (!missingOptionName.isPresent())
			return;

		throw new IllegalArgumentException(String.format("missing required option: %s", missingOptionName.get()));
	}

	private static MultisigOptions Parse(final CommandLine commandLine) {
		final MultisigOptions options = new MultisigOptions();
		options.signer = new Account(new KeyPair(PrivateKey.fromHexString(commandLine.getOptionValue("signer"))));
		options.endpoint = new NodeEndpoint(
				"http",
				commandLine.getOptionValue("host"),
				Integer.parseInt(commandLine.getOptionValue("port")));

		options.n = Integer.parseInt(commandLine.getOptionValue("n"));
		options.m = Integer.parseInt(commandLine.getOptionValue("m"));

		options.cosigners = Arrays.stream(commandLine.getOptionValues("cosigners"))
				.map(s -> new Account(new KeyPair(PublicKey.fromHexString(s))))
				.collect(Collectors.toList());
		return options;
	}

	private Account signer;
	private NodeEndpoint endpoint;
	private int n;
	private int m;
	private Collection<Account> cosigners;
}
