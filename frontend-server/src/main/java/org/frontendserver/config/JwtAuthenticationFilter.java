package org.frontendserver.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private UserServiceClient userServiceClient;

    @Autowired
    public void setUserServiceClient(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.contains("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String cleanedAuthHeader = cleanAuthHeader(authHeader);

            Map<String, Object> validationResult = userServiceClient.validateToken(cleanedAuthHeader);

            if (validationResult != null && Boolean.TRUE.equals(validationResult.get("valid"))) {
                String username = (String) validationResult.get("username");

                List<Map<String, String>> authoritiesList =
                        validationResult.get("authorities") != null ?
                                (List<Map<String, String>>) validationResult.get("authorities") :
                                Collections.emptyList();

                List<SimpleGrantedAuthority> authorities = authoritiesList.stream()
                        .map(map -> new SimpleGrantedAuthority(map.get("authority")))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("Authentication error: ", e);
        }

        filterChain.doFilter(request, response);
    }

    private String cleanAuthHeader(String authHeader) {
        String token = authHeader.replaceAll("(?i)Bearer\\s+", "").trim();

        if (token.length() > 0 && !token.startsWith("ey")) {
            if (token.startsWith("y") && token.length() > 10) {
                token = "e" + token;
            }
        }
        return "Bearer " + token;
    }
}