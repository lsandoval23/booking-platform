package com.booking.auth.config;

import com.booking.utils.JwtUtil;
import com.booking.utils.PasswordUtil;
import com.booking.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {

    @Bean
    JwtUtil jwtUtilBean(@Value("${jwt.secret}") String secretKey,
                        @Value("${jwt.expiration.access}") long accessTokenExpiration,
                        @Value("${jwt.expiration.refresh}") long refreshTokenExpiration) {
        return new JwtUtil(secretKey, accessTokenExpiration, refreshTokenExpiration);
    }

    @Bean
    PasswordUtil passwordUtilBean() {
        return new PasswordUtil();
    }

    @Bean
    ValidationUtil validationUtil() {
        return new ValidationUtil();
    }
}
