package com.kjr.rfp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/resumes/download/**").authenticated()
                        .requestMatchers("/resumes/thanks").permitAll()
                        .requestMatchers("/login", "/resources/**").permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                                .loginPage("/login")
//                        .loginProcessingUrl("/perform_login")
                                .defaultSuccessUrl("/login-success", true) // Always redirect here after login
                                .failureUrl("/login?error=true")
                                .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/resumes/upload")
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("jag")
                .password("Jagadeesh@9395")
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}

