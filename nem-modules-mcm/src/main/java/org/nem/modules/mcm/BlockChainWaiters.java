package org.nem.modules.mcm;

import org.nem.core.model.Address;
import org.nem.core.model.ncc.*;
import org.nem.core.model.primitive.Amount;
import org.nem.core.utils.ExceptionUtils;

import java.util.concurrent.ExecutionException;
import java.util.function.*;

public class BlockChainWaiters {
	private static final int WAIT_SECONDS = 30;

	BlockChainWaiters(final NisConnector connector, final int numWaitSeconds) {
		this.connector = connector;
		this.numWaitSeconds = numWaitSeconds;
	}

	public void waitForBalance(final Address address, final Amount balance) {
		this.waitFor(address, accountMetaDataPair -> accountMetaDataPair.getEntity().getBalance(), balance);
	}

	public void waitForCosigners(final Address address, final int numCosigners) {
		this.waitFor(address, accountMetaDataPair -> accountMetaDataPair.getMetaData().getCosignatories().size(), numCosigners);
	}

	private <T> void waitFor(
			final Address address,
			final Function<AccountMetaDataPair, T> valueAccessor,
			final T expectedValue) {

		for (int i = 0; i < this.numWaitSeconds / WAIT_SECONDS; ++i) {
			if (this.waitOne(address, valueAccessor, expectedValue, i)) {
				return;
			}
		}

		throw new RuntimeException(String.format("failed waiting for value %s", expectedValue));
	}

	private <T> boolean waitOne(
			final Address address,
			final Function<AccountMetaDataPair, T> valueAccessor,
			final T expectedValue,
			final int iteration) {
		final int elapsedSeconds = iteration * WAIT_SECONDS;
		try {
			final AccountMetaDataPair accountMetaDataPair = this.connector.accountInfo(address).get();
			final T value = valueAccessor.apply(accountMetaDataPair);
			if (value.equals(expectedValue)) {
				System.out.println(String.format("done waiting! - value is %s", value));
				return true;
			}

			System.out.println(String.format("still waiting - value is %s (elapsed %ds)", value, elapsedSeconds));
		} catch (InterruptedException | ExecutionException ex) {
			System.out.println(String.format(
					"wait failed - %s [%s] (elapsed %ds)",
					ex.getClass().getName(),
					ex.getMessage(),
					elapsedSeconds));
		}

		ExceptionUtils.propagateVoid(() -> Thread.sleep(WAIT_SECONDS * 1000));
		return false;
	}

	private final NisConnector connector;
	private final int numWaitSeconds;
}
