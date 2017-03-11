package org.nem.modules.mcm;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.*;
import org.nem.core.time.TimeInstant;

import java.util.concurrent.ExecutionException;

public class MultisigCreatorModule implements AutoCloseable {

	public MultisigCreatorModule(
			final MultisigOptions multisigOptions,
			final int waitMinutes) throws ExecutionException, InterruptedException {
		this.multisigOptions = multisigOptions;

		System.out.println("> connecting to remote server");
		this.connector = new NisConnector(multisigOptions.endpoint());
		this.networkTime = this.connector.networkTime().get();
		System.out.println(String.format("version: %s", this.connector.version().get()));
		System.out.println(String.format("nettime: %s", this.networkTime));

		this.builder = new TransactionBuilder(multisigOptions);
		this.waiters = new BlockChainWaiters(this.connector, waitMinutes * 60);
	}

	public KeyPair multisigKeyPair() {
		return this.builder.multisigKeyPair();
	}

	public void create() throws ExecutionException, InterruptedException {
		this.creditFee();
		this.convertToMultisig();
	}

	@Override
	public void close() {
		this.connector.close();
	}

	private void creditFee() throws ExecutionException, InterruptedException {
		System.out.println("> creating fee transfer");
		final TransferTransaction feeTransfer = this.builder.createFeeTransfer(this.networkTime);
		ConsolePrinter.PrintTransaction(feeTransfer);

		System.out.println("> announcing fee transfer");
		this.connector.announce(feeTransfer).get();

		System.out.println("> waiting for balance change");
		this.waiters.waitForBalance(feeTransfer.getRecipient().getAddress(), feeTransfer.getAmount());
	}

	private void convertToMultisig() throws ExecutionException, InterruptedException {
		System.out.println("> creating multisig conversion");
		final Transaction multisigConversion = this.builder.createMultisigConversion(this.networkTime);
		ConsolePrinter.PrintTransaction(multisigConversion);

		System.out.println("> announcing multisig conversion");
		this.connector.announce(multisigConversion).get();

		System.out.println("> waiting for cosigner change");
		this.waiters.waitForCosigners(multisigConversion.getSigner().getAddress(), this.multisigOptions.n());
	}

	private final MultisigOptions multisigOptions;
	private final NisConnector connector;
	private final TimeInstant networkTime;
	private final TransactionBuilder builder;
	private final BlockChainWaiters waiters;
}
