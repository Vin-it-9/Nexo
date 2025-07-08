package org.userservice.config.oauth2;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.userservice.entity.User;
import org.userservice.repository.UserRepository;
import org.userservice.service.JwtService;
import org.userservice.service.RefreshTokenService;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirectUri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String uri = redirectUri;

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Optional<User> userOptional = userRepository.findByEmail(userPrincipal.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String token = jwtService.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user, request).getToken();

            return UriComponentsBuilder.fromUriString(uri)
                    .queryParam("token", token)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();
        }

        return UriComponentsBuilder.fromUriString(uri)
                .queryParam("error", "Authentication failed")
                .build().toUriString();
    }
}