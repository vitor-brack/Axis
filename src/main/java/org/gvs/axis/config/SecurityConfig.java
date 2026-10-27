package org.gvs.axis.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 *
 * @author vitor
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                // Rotas públicas
                .requestMatchers("/", "/home", "/usuario/cadastro", 
                        "/usuario/login", "ambiente/buscar", 
                        "usuario/status",
                        "/css/**", "/js/**", "/img/**").permitAll()
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/usuario/login")
                .loginProcessingUrl("/usuario/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/usuario/login?error=true")
                .usernameParameter("email")
                .passwordParameter("senha")
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/usuario/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(new BCryptPasswordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Component
    class AuthenticationInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                HttpServletResponse response,
                Object handler) throws Exception {

            String path = request.getRequestURI();
            boolean isAuthenticated = SecurityContextHolder.getContext()
                    .getAuthentication() != null
                    && SecurityContextHolder.getContext()
                            .getAuthentication().isAuthenticated()
                    && !SecurityContextHolder.getContext()
                            .getAuthentication().getPrincipal()
                            .equals("anonymousUser");

            boolean isAuthPage = path.equals("/usuario/login")
                    || path.equals("/usuario/cadastro");

            if (isAuthenticated && isAuthPage) {
                response.sendRedirect("/");
                return false;
            }

            return true;
        }
    }
}
