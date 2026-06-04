package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {
	
    @GetMapping("/login")
    public String loginPage() {
        return "login";   // busca login.html en templates
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password"; // Carga el template forgot-password.html
    }

    @GetMapping("/reset-password")
    public ModelAndView showResetPasswordForm(@RequestParam("token") String token) {
        ModelAndView modelAndView = new ModelAndView("reset-password");
        modelAndView.addObject("token", token);
        return modelAndView;
    }
    
}