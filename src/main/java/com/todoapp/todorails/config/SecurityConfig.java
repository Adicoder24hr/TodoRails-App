package com.todoapp.todorails.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/subscribe"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/css/**", "/js/**","/subscribe","/sw.js").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/todos", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL for logout
                        .logoutSuccessUrl("/login?logout") // redirect after logout
                        .invalidateHttpSession(true) // clear session
                        .deleteCookies("JSESSIONID") // delete session cookie
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1) // allow only 1 session per user
                        .maxSessionsPreventsLogin(false)
                )
                .sessionManagement(session -> session
                        .invalidSessionUrl("/login?session=expired") // redirect if expired
                        .sessionFixation().migrateSession()
                );

        return http.build();
    }
}
