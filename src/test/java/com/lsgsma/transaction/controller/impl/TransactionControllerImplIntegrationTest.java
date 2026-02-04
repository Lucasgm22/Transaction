package com.lsgsma.transaction.controller.impl;

import tools.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.model.Transaction;
import com.lsgsma.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class TransactionControllerImplIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private TransactionRepository transactionRepository;

    @Autowired
    private CacheManager cacheManager;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("api.treasury.base-url", wireMockServer::baseUrl);
    }


    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    @Test
    void givenValidRequest_whenStoreTransaction_thenReturns201() throws Exception {
        var requestDto = new CreateTransactionRequest(
                "New MacBook Pro",
                LocalDate.now(),
                BigDecimal.valueOf(2500.00)
        );

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void givenInvalidDescription_whenStoreTransaction_thenReturns400() throws Exception {
        String longDescription = "a".repeat(51);
        var requestDto = new CreateTransactionRequest(longDescription, LocalDate.now(), BigDecimal.valueOf(100));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.description").value("size must be between 0 and 50"))
                .andExpect(jsonPath("$.path").value("/transaction"));
    }

    @Test
    void givenInvalidAmount_whenStoreTransaction_thenReturns400() throws Exception {
        var requestDto = new CreateTransactionRequest("description", LocalDate.now(), BigDecimal.ZERO);

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.purchaseAmount").value("must be greater than 0"))
                .andExpect(jsonPath("$.path").value("/transaction"));
    }

    @Test
    void givenInvalidTransactionDate_whenStoreTransaction_thenReturns400() throws Exception {
        var requestDto = new CreateTransactionRequest("description", LocalDate.now().plusDays(1), BigDecimal.valueOf(100));

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.transactionDate").value("must be a date in the past or in the present"))
                .andExpect(jsonPath("$.path").value("/transaction"));
    }

    @Test
    void givenCompletelyInvalidTransaction_whenStoreTransaction_thenReturns400AndDescribeAllFields() throws Exception {
        var requestDto = new CreateTransactionRequest("a".repeat(51), LocalDate.now().plusDays(1), BigDecimal.ZERO);

        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(3)))
                .andExpect(jsonPath("$.messages.description").value("size must be between 0 and 50"))
                .andExpect(jsonPath("$.messages.purchaseAmount").value("must be greater than 0"))
                .andExpect(jsonPath("$.messages.transactionDate").value("must be a date in the past or in the present"))
                .andExpect(jsonPath("$.path").value("/transaction"));
    }

    @Test
    void givenValidIdAndCurrency_whenGetConverted_thenReturns200() throws Exception {

        var transaction = new Transaction();
        transaction.setDescription("Test Purchase");
        transaction.setPurchaseAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(LocalDate.of(2024, 8, 20));
        var savedTransaction = transactionRepository.save(transaction);
        var transactionId = savedTransaction.getId();

        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/accounting/od/rates_of_exchange.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("mock/treasury-exchange-rate-response-with-data.json"))));

        mockMvc.perform(get("/transaction/{id}", transactionId)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.description").value("Test Purchase"))
                .andExpect(jsonPath("$.transactionDate").value("2024-08-20"))
                .andExpect(jsonPath("$.originalPurchaseAmount").value(100.00))
                .andExpect(jsonPath("$.exchangeRate").value(5.5))
                .andExpect(jsonPath("$.convertedAmount").value(550.00));

    }

    @Test
    void givenNonExistentId_whenGetConverted_thenReturns404() throws Exception {
        var nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/transaction/{id}", nonExistentId)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.resourceNotFound").value("Transaction not found with id: " + nonExistentId))
                .andExpect(jsonPath("$.path").value("/transaction/" + nonExistentId));
    }

    @Test
    void givenInvalidUUId_whenGetConverted_thenReturns400() throws Exception {
        var invalidId = "invalid";

        mockMvc.perform(get("/transaction/{id}", invalidId)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Request Value error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.requestValue").value("Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: " + invalidId))
                .andExpect(jsonPath("$.path").value("/transaction/" + invalidId));
    }

    @Test
    void givenInvalidCurrency_whenGetConverted_thenReturns400() throws Exception {
        var id = UUID.randomUUID();

        mockMvc.perform(get("/transaction/{id}", id)
                        .param("currency", "Brazil-</Real>"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Request Value error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.requestValue").value("Currency format is invalid or contains prohibited characters."))
                .andExpect(jsonPath("$.path").value("/transaction/" + id));
    }

    @Test
    void givenValidIdAndCurrency_whenNoExchangeRate_thenReturns404() throws Exception {
        var transaction = new Transaction();
        transaction.setDescription("Test Purchase");
        transaction.setPurchaseAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(LocalDate.of(2024, 8, 20));
        var savedTransaction = transactionRepository.save(transaction);
        var transactionId = savedTransaction.getId();

        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/accounting/od/rates_of_exchange.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readStringFromFile("mock/treasury-exchange-rate-response-without-data.json"))));

        mockMvc.perform(get("/transaction/{id}", transactionId)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.resourceNotFound").value("Could not retrieve exchange rates for Brazil-Real"))
                .andExpect(jsonPath("$.path").value("/transaction/" + transactionId));

    }

    @Test
    void givenValidIdAndCurrency_whenCantGetResponseFromTreasureApi_thenReturns404() throws Exception {
        var transaction = new Transaction();
        transaction.setDescription("Test Purchase");
        transaction.setPurchaseAmount(BigDecimal.valueOf(100.00));
        transaction.setTransactionDate(LocalDate.of(2024, 8, 20));
        var savedTransaction = transactionRepository.save(transaction);
        var transactionId = savedTransaction.getId();

        wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/v1/accounting/od/rates_of_exchange.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"NOT FOUND\"}")));

        mockMvc.perform(get("/transaction/{id}", transactionId)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.resourceNotFound").value("Could not retrieve exchange rates for Brazil-Real"))
                .andExpect(jsonPath("$.path").value("/transaction/" + transactionId));

    }

    @Test
    void givenValidRequest_whenUnhandledException_thenReturns500() throws Exception {
        var id = UUID.randomUUID();

        when(transactionRepository.findById(id)).thenThrow(new RuntimeException("Just a test"));

        mockMvc.perform(get("/transaction/{id}", id)
                        .param("currency", "Brazil-Real"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.internalError").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.path").value("/transaction/" + id));
    }

    @Test
    void givenInvalidPath_whenCalled_thenReturns404() throws Exception {
        mockMvc.perform(get("/not-mapped"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.messages", aMapWithSize(1)))
                .andExpect(jsonPath("$.messages.resourceNotFound").value("No static resource not-mapped for request '/not-mapped'."))
                .andExpect(jsonPath("$.path").value("/not-mapped"));
    }

    private String readStringFromFile(final String path) {
        try {
            return new String(Objects
                    .requireNonNull(getClass().getClassLoader().getResourceAsStream(path))
                    .readAllBytes()
            );
        } catch (Exception _) {
            throw new RuntimeException("Cound not read file: " + path);
        }
    }

}