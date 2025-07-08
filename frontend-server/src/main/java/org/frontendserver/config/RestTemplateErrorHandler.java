package org.frontendserver.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Component
public class RestTemplateErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        if (response.getStatusCode().is5xxServerError()) {
            try {
                String body = new BufferedReader(new InputStreamReader(response.getBody()))
                        .lines()
                        .collect(Collectors.joining("\n"));

                System.err.println("Server error response: " + body);
            } catch (Exception e) {
                System.err.println("Error reading response body: " + e.getMessage());
            }
        }

        super.handleError(response);
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return super.hasError(response);
    }
}