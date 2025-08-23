package com.lsgsma.transaction.controller;

import com.lsgsma.transaction.dto.request.CreateTransactionRequest;
import com.lsgsma.transaction.dto.response.ConvertedTransactionResponse;
import com.lsgsma.transaction.dto.response.CreateTransactionResponse;
import com.lsgsma.transaction.dto.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Transaction Management", description = "APIs for managing purchase transactions")
public interface TransactionController {

    @Operation(summary = "Store a new transaction",
            description = "Accepts a transaction and persists it in the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CreateTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    CreateTransactionResponse storeTransaction(@Valid @RequestBody final CreateTransactionRequest request);

    @Operation(summary = "Retrieve a transaction in a specified currency",
            description = "Fetches a stored transaction by its ID and converts the purchase purchaseAmount to the target currency.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction found and converted",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConvertedTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transaction not found or exchange rate not available for the given date",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ConvertedTransactionResponse getConvertedTransaction(
            @Parameter(description = "Unique identifier of the transaction (UUID)", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
            @PathVariable
            final UUID id,

            @Parameter(description = "Target currency for conversion, if not given no conversion is made", example = "Brazil-Real")
            @Pattern(
                    regexp = "^[^<>\"]+-[^<>\"]+$",
                    message = "Currency format is invalid or contains prohibited characters."
            )
            @RequestParam(required = false)
            final String currency
    );
}
