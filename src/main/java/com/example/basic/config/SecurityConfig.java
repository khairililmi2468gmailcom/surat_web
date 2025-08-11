package com.example.basic.config;

import com.example.basic.model.User;
import com.example.basic.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Inject UserRepository to manage users from Google login
    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/auth/login"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/contact", "/auth/login", "/auth/register", "/css/**", "/js/**",
                                "/images/**", "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(successHandler())
                        .failureHandler(failureHandler())
                        .permitAll())
                // --- BARU: Tambahkan Konfigurasi OAuth2 Login ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login") // Arahkan ke login page jika otentikasi diperlukan
                        .successHandler(oAuth2LoginSuccessHandler()) // Handler setelah login Google berhasil
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll());

        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler oAuth2LoginSuccessHandler() {
        PasswordEncoder passwordEncoder = passwordEncoder(); 

        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            String email = attributes.get("email").toString();
            String name = attributes.get("name").toString();

            userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                
                String username = name;
                if (userRepository.findByUsername(username).isPresent()) {
                    username = username + ThreadLocalRandom.current().nextInt(100, 1000); 
                }
                newUser.setUsername(username);
                
                String randomPassword = UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(randomPassword));
                
                newUser.setRole("ROLE_USER");
                newUser.setProvider("GOOGLE"); // <-- SET PROVIDER KE GOOGLE
                
                return userRepository.save(newUser);
            });

            response.sendRedirect("/dashboard");
        };
    }
    // Handler untuk form login standar (tetap sama)
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), Map.of("success", true, "redirectUrl", "/dashboard"));
        };
    }

    // Handler untuk form login gagal (tetap sama)
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            String email = request.getParameter("username"); // 'username' adalah nama field email di form
            String errorMessage = "Email atau password salah.";

            // Cari user berdasarkan email yang diinput
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Jika user ada dan providernya adalah GOOGLE, beri pesan khusus
                if ("GOOGLE".equals(user.getProvider())) {
                    errorMessage = "Akun ini terdaftar melalui Google. Silakan login dengan Google.";
                }
            }

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(),
                    Map.of("success", false, "message", errorMessage));
        };
    }

    // UserDetailsService dan PasswordEncoder (tetap sama)
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            User user = userRepository.findByEmail(email)
                    .filter(u -> u.getPassword() != null) // Hanya cari user dengan password (untuk form login)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}