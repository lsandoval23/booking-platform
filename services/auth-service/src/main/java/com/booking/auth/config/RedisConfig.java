package com.booking.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for token blacklist management.
 * 
 * Redis is used to store:
 * - Blacklisted JWT tokens (after logout)
 * - Refresh tokens
 * - Session data (if needed)
 * 
 * The RedisTemplate is configured with proper serializers to handle
 * String keys and JSON values efficiently.
 * 
 * @author Booking Platform Team
 * @version 1.0.0
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate for storing and retrieving data from Redis.
     * 
     * Key Serializer: StringRedisSerializer
     * - Used for Redis keys (e.g., "blacklist:token123")
     * 
     * Value Serializer: GenericJackson2JsonRedisSerializer
     * - Used for Redis values (supports complex objects)
     * 
     * Hash Key/Value Serializers: Same as above
     * - Used for Redis hash operations
     * 
     * @param connectionFactory Redis connection factory (auto-configured by Spring Boot)
     * @return Configured RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // Set connection factory
        template.setConnectionFactory(connectionFactory);
        
        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        // Key serializer (for simple string keys)
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value serializer (for JSON values)
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        // Enable transaction support (optional)
        template.setEnableTransactionSupport(true);
        
        // Initialize template
        template.afterPropertiesSet();
        
        return template;
    }


}
