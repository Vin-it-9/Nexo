package org.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.dto.PriceResponse;
import org.service.PriceService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Path("/api/prices")
@Produces(MediaType.APPLICATION_JSON)
@Slf4j
@Tag(name = "Crypto Price Feed", description = "Operations for fetching cryptocurrency price data")
public class PriceController {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));

    @Inject
    PriceService priceService;

    @GET
    @Operation(summary = "Get all crypto prices", description = "Returns the latest prices for all available cryptocurrencies")
    @APIResponse(responseCode = "200", description = "List of current prices",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PriceResponse.class)))
    public Response getAllPrices() {
        List<PriceResponse> prices = priceService.getCurrentPrices();
        return Response.ok(prices).build();
    }

    @GET
    @Path("/id/{id}")
    @Operation(summary = "Get price by ID", description = "Fetch the price of a cryptocurrency using its ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Price data found",
                    content = @Content(schema = @Schema(implementation = PriceResponse.class))),
            @APIResponse(responseCode = "404", description = "Price data not found")
    })
    public Response getPriceById(
            @Parameter(description = "The unique ID of the cryptocurrency") @PathParam("id") String id) {

        PriceResponse price = priceService.getPriceById(id);
        if (price == null) {
            throw new NotFoundException("Price data not found for id: " + id);
        }

        return Response.ok(price).build();
    }

    @GET
    @Path("/symbol/{symbol}")
    @Operation(summary = "Get price by symbol", description = "Fetch the price of a cryptocurrency using its symbol (e.g., BTC)")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Price data found",
                    content = @Content(schema = @Schema(implementation = PriceResponse.class))),
            @APIResponse(responseCode = "404", description = "Price data not found")
    })
    public Response getPriceBySymbol(
            @Parameter(description = "The symbol of the cryptocurrency") @PathParam("symbol") String symbol) {

        PriceResponse price = priceService.getPriceBySymbol(symbol);
        if (price == null) {
            throw new NotFoundException("Price data not found for symbol: " + symbol);
        }

        return Response.ok(price).build();
    }

    @GET
    @Path("/refresh")
    @Operation(summary = "Refresh price data manually", description = "Fetch latest prices from external provider (e.g., CoinMarketCap)")
    @APIResponse(responseCode = "200", description = "Price data refreshed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PriceResponse.class)))
    public Response refreshPrices() {
        List<PriceResponse> prices = priceService.refreshPrices();
        return Response.ok(prices).build();
    }

    @GET
    @Path("/cache/info")
    @Operation(summary = "Get cache status information", description = "Returns information about the cache including last fetch time and user details")
    @APIResponse(responseCode = "200", description = "Cache information",
            content = @Content(mediaType = "application/json"))
    public Response getCacheInfo() {
        Map<String, Object> cacheInfo = priceService.getCacheInfo();

        String currentTimeUTC = FORMATTER.format(Instant.now());

        Map<String, Object> response = Map.of(
                "currentDateAndTimeUTC", currentTimeUTC,
                "cacheDetails", cacheInfo
        );

        return Response.ok(response).build();
    }
}