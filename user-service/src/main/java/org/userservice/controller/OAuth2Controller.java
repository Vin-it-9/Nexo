package org.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    @GetMapping("/urls")
    public Object getOAuthUrls() {
        return new Object() {
            public final String google = "/oauth2/authorize/google";
        };
    }
}