package org.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.dto.PriceResponse;
import org.service.MarketAnalysisService;
import org.service.PriceService;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;

@Path("/api/market")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Market Analysis", description = "Advanced cryptocurrency market analytics endpoints")
@Slf4j
public class EnhancedPriceController {

    @Inject
    PriceService priceService;

    @Inject
    MarketAnalysisService marketAnalysisService;

    @GET
    @Path("/top-gainers")
    @Operation(summary = "Get top gaining cryptocurrencies",
            description = "Returns the cryptocurrencies with the highest positive price change in the last 24 hours")
    @APIResponse(responseCode = "200", description = "List of top gainers",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PriceResponse.class, type = SchemaType.ARRAY)))
    public Response getTopGainers(
            @Parameter(description = "Number of top gainers to return")
            @QueryParam("limit") @DefaultValue("5") int limit) {
        List<PriceResponse> topGainers = marketAnalysisService.getTopGainers(limit);
        return Response.ok(topGainers).build();
    }

    @GET
    @Path("/top-losers")
    @Operation(summary = "Get top losing cryptocurrencies",
            description = "Returns the cryptocurrencies with the largest negative price change in the last 24 hours")
    @APIResponse(responseCode = "200", description = "List of top losers",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PriceResponse.class, type = SchemaType.ARRAY)))
    public Response getTopLosers(
            @Parameter(description = "Number of top losers to return")
            @QueryParam("limit") @DefaultValue("5") int limit) {
        List<PriceResponse> topLosers = marketAnalysisService.getTopLosers(limit);
        return Response.ok(topLosers).build();
    }

    @GET
    @Path("/market-cap-distribution")
    @Operation(summary = "Get market cap distribution",
            description = "Returns the percentage distribution of market capitalization among top cryptocurrencies")
    @APIResponse(responseCode = "200", description = "Market cap distribution by cryptocurrency",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type = SchemaType.OBJECT,
                            implementation = Map.class)))
    public Response getMarketCapDistribution() {
        Map<String, Double> distribution = marketAnalysisService.getMarketCapDistribution();
        return Response.ok(distribution).build();
    }

    @GET
    @Path("/volume-leaders")
    @Operation(summary = "Get trading volume leaders",
            description = "Returns cryptocurrencies with the highest trading volume in the last 24 hours")
    @APIResponse(responseCode = "200", description = "List of volume leaders",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PriceResponse.class, type = SchemaType.ARRAY)))
    public Response getVolumeLeaders(
            @Parameter(description = "Number of volume leaders to return")
            @QueryParam("limit") @DefaultValue("5") int limit) {
        List<PriceResponse> volumeLeaders = marketAnalysisService.getVolumeLeaders(limit);
        return Response.ok(volumeLeaders).build();
    }

    @GET
    @Path("/price-alerts/{symbol}/{threshold}")
    @Operation(summary = "Create price alert",
            description = "Sets up a price alert for a specific cryptocurrency at the given threshold")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Alert created successfully",
                    content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "Cryptocurrency symbol not found")
    })
    public Response setPriceAlert(
            @Parameter(description = "Cryptocurrency symbol (e.g., BTC)")
            @PathParam("symbol") String symbol,

            @Parameter(description = "Price threshold for the alert")
            @PathParam("threshold") double threshold,

            @Parameter(description = "Alert direction - 'above' or 'below' the threshold")
            @QueryParam("direction") @DefaultValue("above") String direction) {
        boolean result = marketAnalysisService.createPriceAlert(symbol, threshold, direction);
        return Response.ok(Map.of("success", result)).build();
    }
}