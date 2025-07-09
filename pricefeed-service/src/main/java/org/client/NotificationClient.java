package org.client;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.dto.NotificationRequest;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/notifications")
@RegisterRestClient(configKey = "notification-api")
public interface NotificationClient {

    @POST
    @Path("/send")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Response sendNotification(NotificationRequest request);
}