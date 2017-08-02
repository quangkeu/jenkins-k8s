package vn.vnpt.ssdc.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by vietnq on 11/13/16.
 */
@Controller
public class LoginController {

    @Value("${identityWebApplicationUrl}")
    public String identityWebApplicationUrl;

    @GetMapping("/login")
    public String login() {
        return "login";
        //return "redirect:" + identityWebApplicationUrl;
    }
}
