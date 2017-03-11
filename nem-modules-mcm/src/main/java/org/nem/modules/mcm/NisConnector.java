package org.nem.modules.mcm;

import org.nem.core.connect.*;
import org.nem.core.connect.client.*;
import org.nem.core.crypto.*;
import org.nem.core.model.*;
import org.nem.core.model.ncc.*;
import org.nem.core.node.*;
import org.nem.core.serialization.*;
import org.nem.core.time.TimeInstant;
import org.nem.core.time.synchronization.CommunicationTimeStamps;

import java.util.concurrent.*;

public class NisConnector {

	public NisConnector(final NodeEndpoint endpoint) {
		this.endpoint = endpoint;
		final HttpMethodClient<ErrorResponseDeserializerUnion> httpMethodClient = CreateHttpMethodClient();
		this.connector = new DefaultAsyncNemConnector<>(
				httpMethodClient,
				r -> { throw new RuntimeException(r.getError()); });
		this.connector.setAccountLookup(null);
	}

	// region get

	public CompletableFuture<NodeVersion> version() {
		return this.getAsync(NisApiId.NIS_REST_NODE_INFO)
				.thenApply(d -> new Node(d).getMetaData().getVersion());
	}

	public CompletableFuture<TimeInstant> networkTime() {
		return this.getAsync(NisApiId.NIS_REST_TIME_SYNC_NETWORK_TIME)
				.thenApply(d -> {
					final long rawTime = new CommunicationTimeStamps(d).getSendTimeStamp().getRaw() / 1000;
					return new TimeInstant((int) rawTime);
				});
	}

	public CompletableFuture<AccountMetaDataPair> accountInfo(final Address address) {
		return this.getAsync(NisApiId.NIS_REST_ACCOUNT_LOOK_UP, String.format("address=%s", address))
				.thenApply(AccountMetaDataPair::new);
	}

	// endregion

	// region post

	public CompletableFuture announce(final Transaction transaction) {
		final byte[] transactionBytes = BinarySerializer.serializeToBytes(transaction.asNonVerifiable());
		final RequestPrepare preparedTransaction = new RequestPrepare(transactionBytes);

		final Signer signer = transaction.getSigner().createSigner();
		final RequestAnnounce announce = new RequestAnnounce(
				preparedTransaction.getData(),
				signer.sign(preparedTransaction.getData()).getBytes());

		return this.post(NisApiId.NIS_REST_TRANSACTION_ANNOUNCE, new HttpBinaryPostRequest(announce));
	}

	// endregion

	private CompletableFuture<Deserializer> getAsync(final NisApiId apiId) {
		return this.getAsync(apiId, null);
	}

	private CompletableFuture<Deserializer> getAsync(final NisApiId apiId, final String query) {
		return this.connector.getAsync(this.endpoint, apiId.toString(), query);
	}

	private CompletableFuture<Deserializer> post(final NisApiId apiId, final HttpPostRequest postRequest) {
		return this.connector.postAsync(this.endpoint, apiId.toString(), postRequest);
	}

	private static HttpMethodClient<ErrorResponseDeserializerUnion> CreateHttpMethodClient() {
		final int CONNECTION_TIMEOUT = 2000;
		final int SOCKET_TIMEOUT = 2000;
		final int REQUEST_TIMEOUT = 4000;
		return new HttpMethodClient<>(CONNECTION_TIMEOUT, SOCKET_TIMEOUT, REQUEST_TIMEOUT);
	}

	private final NodeEndpoint endpoint;
	private final DefaultAsyncNemConnector<String> connector;
}
