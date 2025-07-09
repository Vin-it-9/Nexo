package org.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.dto.CoinMarketCapResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v1")
@RegisterRestClient(configKey = "coinmarketcap-api")
public interface CoinMarketCapClient {

    @GET
    @Path("/cryptocurrency/listings/latest")
    CoinMarketCapResponse getLatestListings(
            @HeaderParam("X-CMC_PRO_API_KEY") String apiKey,
            @QueryParam("start") int start,
            @QueryParam("limit") int limit,
            @QueryParam("convert") String convert
    );
}