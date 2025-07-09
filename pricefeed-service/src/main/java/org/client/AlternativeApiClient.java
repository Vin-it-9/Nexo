package org.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.dto.PriceResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

@Path("/api/v1")
@RegisterRestClient(configKey = "alternative-api")
public interface AlternativeApiClient {

    @GET
    @Path("/prices")
    @Produces(MediaType.APPLICATION_JSON)
    List<PriceResponse> getPrices(@HeaderParam("X-API-KEY") String apiKey);
}