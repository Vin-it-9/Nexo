
package org.frontendserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class WebController {


    @GetMapping("/")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/oauth2/redirect")
    public String oauth2RedirectPage() {
        return "oauth2/redirect";
    }

    @GetMapping("/account")
    public String accountPage() {
        return "account/profile";
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "dashboard";
    }

}