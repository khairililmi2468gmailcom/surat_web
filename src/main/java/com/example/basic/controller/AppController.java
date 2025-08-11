package com.example.basic.controller;

import com.example.basic.model.Message;
import com.example.basic.model.User;
import com.example.basic.repository.MessageRepository;
import com.example.basic.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

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
    
    @GetMapping("/contact")
    public String showContactPage(Model model){
        model.addAttribute("message", new Message());
        return "pages/contact";
    }
   
    @PostMapping("/contact")
    public String submitContactForm(@ModelAttribute("message") Message message, RedirectAttributes redirectAttributes){
        messageRepository.save(message);
        redirectAttributes.addFlashAttribute("successMessage", "Terima kasih, pesan Anda telah terkirim!");
        return "redirect:/contact";
    }

    @GetMapping("/auth/login")
    public String showLoginPage(Model model) {
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String showRegisterPage(Model model) {
        return "auth/register";
    }

    /**
     * Menangani registrasi pengguna baru via form (AJAX).
     * Method ini mengembalikan ResponseEntity dalam format JSON.
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestParam String username,
                                                              @RequestParam String email,
                                                              @RequestParam String password) {
        // 1. Cek duplikasi email
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // HTTP 409: Konflik
                    .body(Map.of("success", false, "message", "Email sudah terdaftar. Silakan gunakan email lain."));
        }
        
        // 2. Cek duplikasi username
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT) // HTTP 409: Konflik
                    .body(Map.of("success", false, "message", "Username sudah digunakan. Silakan pilih username lain."));
        }
        
        // Buat dan simpan pengguna baru
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Gunakan bean PasswordEncoder
        newUser.setProvider("LOCAL"); // Set provider secara eksplisit
        
        userRepository.save(newUser);

        // Kirim respons sukses
        return ResponseEntity
                .ok(Map.of("success", true, "message", "Registrasi berhasil! Anda akan dialihkan ke halaman login."));
    }

    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); 
        
        // Ambil detail user dari database untuk mendapatkan username
        userRepository.findByEmail(userEmail).ifPresent(user -> {
            model.addAttribute("userEmail", user.getEmail());
            model.addAttribute("username", user.getUsername());
        });
        
        return "dashboard";
    }
}