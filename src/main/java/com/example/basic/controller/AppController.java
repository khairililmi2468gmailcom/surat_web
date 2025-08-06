package com.example.basic.controller;

import com.example.basic.model.Message;
import com.example.basic.model.User;
import com.example.basic.repository.MessageRepository;
import com.example.basic.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/")
    public String showLandingPage(){
        return "landing-page/index";
    }
    // route kontak
    // Method GET hanya untuk menampilkan form
    @GetMapping("contact")
    public String showContactPage(Model model){
        model.addAttribute("message", new Message());
        return "pages/contact";
    }
   
    // Method POST hanya untuk memproses data form
    @PostMapping("contact")
    public String submitContactForm(@ModelAttribute("message") Message message, RedirectAttributes redirectAttributes){
        messageRepository.save(message);
        redirectAttributes.addFlashAttribute("successMessage", "Terima kasih, pesan Anda telah terkirim!");
        return "redirect:/contact";
    }




    @GetMapping("auth/login")
    public String showLoginPage(Model model) {
        // Langsung tampilkan halaman login, tidak lagi menggunakan layout
        return "auth/login";
    }

    @GetMapping("auth/register")
    public String showRegisterPage(Model model) {
        // Langsung tampilkan halaman register
        return "auth/register";
    }

    @PostMapping("register")
    public String registerUser(@ModelAttribute User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/auth/login";
    }

    @GetMapping("dashboard")
    public String showDashboardPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        model.addAttribute("username", username);
        // Langsung tampilkan halaman dashboard
        return "dashboard";
    }
}