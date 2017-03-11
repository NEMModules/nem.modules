package org.nem.modules.mcm;

import org.nem.core.crypto.KeyPair;
import org.nem.core.model.*;
import org.nem.core.model.primitive.BlockHeight;
import org.nem.core.time.TimeInstant;

import java.util.Collection;
import java.util.stream.Collectors;

public class TransactionBuilder {

	public KeyPair multisigKeyPair() {
		return this.multisigKeyPair;
	}

	public TransactionBuilder(final MultisigOptions options) {
		this.options = options;
		this.multisigKeyPair = new KeyPair();
	}

	public TransferTransaction createFeeTransfer(final TimeInstant timeStamp) {
		final TransferTransaction transaction = new TransferTransaction(
				timeStamp,
				this.options.signer(),
				new Account(this.multisigKeyPair),
				this.createMultisigConversion(timeStamp).getFee(),
				null);
		return PrepareTransaction(transaction);
	}

	public MultisigAggregateModificationTransaction createMultisigConversion(final TimeInstant timeStamp) {
		final Collection<MultisigCosignatoryModification> cosignatoryModifications = this.options.cosigners().stream()
				.map(account -> new MultisigCosignatoryModification(
						MultisigModificationType.AddCosignatory,
						account))
				.collect(Collectors.toList());
		final MultisigAggregateModificationTransaction transaction = new MultisigAggregateModificationTransaction(
				timeStamp,
				new Account(this.multisigKeyPair),
				cosignatoryModifications,
				new MultisigMinCosignatoriesModification(this.options.m()));
		return PrepareTransaction(transaction);
	}

	private static <TTransaction extends Transaction> TTransaction PrepareTransaction(final TTransaction transaction) {
		final DefaultTransactionFeeCalculator feeCalculator = new DefaultTransactionFeeCalculator(
			id -> null,
			() -> BlockHeight.ONE,
			BlockHeight.MAX);

		transaction.setFee(feeCalculator.calculateMinimumFee(transaction));
		transaction.setDeadline(transaction.getTimeStamp().addHours(1));
		transaction.sign();
		return transaction;
	}

	private final MultisigOptions options;
	private final KeyPair multisigKeyPair;
}
