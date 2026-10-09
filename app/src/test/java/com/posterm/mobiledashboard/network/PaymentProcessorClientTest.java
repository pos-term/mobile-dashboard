package com.posterm.mobiledashboard.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.posterm.mobiledashboard.model.ProblemCode;
import com.posterm.mobiledashboard.model.ProblemDetails;
import com.posterm.mobiledashboard.model.TerminalListResponse;
import com.posterm.mobiledashboard.model.Transaction;
import com.posterm.mobiledashboard.model.TransactionPage;
import com.posterm.mobiledashboard.model.TransactionStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Response;

public final class PaymentProcessorClientTest {
    private MockWebServer server;
    private PaymentProcessorClient client;

    @Before
    public void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = PaymentProcessorClient.create(
                new ApiConfiguration(server.url("/").toString(), "test-token")
        );
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    public void listTransactionsSendsFiltersAndBearerToken() throws Exception {
        enqueueJson(200, "{"
                + "\"items\":[{"
                + "\"transaction_id\":\"0192f0c4-7a3e-7b21-9d4e-2c1f8a6b5e10\","
                + "\"terminal_id\":\"term-001\","
                + "\"amount_minor\":15050,"
                + "\"currency\":\"RUB\","
                + "\"status\":\"failed\","
                + "\"reason\":\"LIMIT_EXCEEDED\","
                + "\"created_at\":\"2026-09-19T10:15:30.123Z\","
                + "\"processed_at\":\"2026-09-19T10:15:31.005Z\""
                + "}],"
                + "\"next_cursor\":\"cursor-2\""
                + "}");

        Response<TransactionPage> response = client.getApi().listTransactions(
                "term-001",
                TransactionStatus.FAILED,
                "2026-09-01T00:00:00Z",
                "2026-10-01T00:00:00Z",
                25,
                "cursor-1"
        ).execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals(1, response.body().getItems().size());
        assertEquals("cursor-2", response.body().getNextCursor());
        assertEquals(TransactionStatus.FAILED, response.body().getItems().get(0).getStatus());

        RecordedRequest request = server.takeRequest();
        assertEquals("Bearer test-token", request.getHeader("Authorization"));
        HttpUrl url = request.getRequestUrl();
        assertNotNull(url);
        assertEquals("/v1/transactions", url.encodedPath());
        assertEquals("term-001", url.queryParameter("terminal_id"));
        assertEquals("failed", url.queryParameter("status"));
        assertEquals("25", url.queryParameter("limit"));
        assertEquals("cursor-1", url.queryParameter("cursor"));
    }

    @Test
    public void getTransactionDeserializesTransaction() throws Exception {
        enqueueJson(200, "{"
                + "\"transaction_id\":\"tx-123\","
                + "\"terminal_id\":\"term-002\","
                + "\"amount_minor\":9900,"
                + "\"currency\":\"RUB\","
                + "\"status\":\"success\","
                + "\"reason\":null,"
                + "\"created_at\":\"2026-09-20T10:00:00.000Z\","
                + "\"processed_at\":\"2026-09-20T10:00:01.000Z\""
                + "}");

        Response<Transaction> response = client.getApi().getTransaction("tx-123").execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals("tx-123", response.body().getTransactionId());
        assertEquals("term-002", response.body().getTerminalId());
        assertEquals(9900L, response.body().getAmountMinor());
        assertEquals(TransactionStatus.SUCCESS, response.body().getStatus());
        assertNull(response.body().getReason());
        assertEquals("/v1/transactions/tx-123", server.takeRequest().getPath());
    }

    @Test
    public void listTerminalsDeserializesItems() throws Exception {
        enqueueJson(200, "{\"items\":[{"
                + "\"terminal_id\":\"term-003\","
                + "\"last_transaction_at\":\"2026-09-21T12:00:00.000Z\""
                + "}]}");

        Response<TerminalListResponse> response = client.getApi().listTerminals().execute();

        assertTrue(response.isSuccessful());
        assertNotNull(response.body());
        assertEquals(1, response.body().getItems().size());
        assertEquals("term-003", response.body().getItems().get(0).getTerminalId());
        assertEquals("/v1/terminals", server.takeRequest().getPath());
    }

    @Test
    public void parseErrorReadsProblemDetails() throws Exception {
        enqueueProblem(404, "{"
                + "\"type\":\"about:blank\","
                + "\"title\":\"Transaction not found\","
                + "\"status\":404,"
                + "\"detail\":\"No transaction with the requested id\","
                + "\"code\":\"not_found\""
                + "}");

        Response<Transaction> response = client.getApi().getTransaction("missing").execute();
        ProblemDetails problem = client.parseError(response);

        assertFalse(response.isSuccessful());
        assertNotNull(problem);
        assertEquals(404, problem.getStatus());
        assertEquals(ProblemCode.NOT_FOUND, problem.getCode());
        assertEquals("Transaction not found", problem.getTitle());
    }

    @Test
    public void configurationNormalizesBaseUrl() {
        ApiConfiguration configuration = new ApiConfiguration(
                "https://processor.example/v1",
                " token "
        );

        assertEquals("https://processor.example/v1/", configuration.getBaseUrl());
        assertEquals("token", configuration.getBearerToken());
    }

    @Test(expected = IllegalArgumentException.class)
    public void configurationRejectsBlankToken() {
        new ApiConfiguration("https://processor.example/", "  ");
    }

    private void enqueueJson(int statusCode, String body) {
        server.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private void enqueueProblem(int statusCode, String body) {
        server.enqueue(new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Type", "application/problem+json")
                .setBody(body));
    }
}
