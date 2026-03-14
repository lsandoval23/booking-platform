package com.booking.auth.repository;

import com.booking.auth.domain.Role;
import com.booking.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User} entity.
 * Provides CRUD operations and custom query methods for user management.
 * 
 * <p>This repository follows Spring Data JPA naming conventions for automatic
 * query generation. All methods are automatically implemented at runtime by
 * Spring Data JPA.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Email-based user lookup for authentication</li>
 *   <li>Email existence check for registration validation</li>
 *   <li>Role-based user queries for admin operations</li>
 * </ul>
 * 
 * @see User
 * @see Role
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their email address.
     * 
     * <p>This method is primarily used during authentication to retrieve user
     * credentials. The email field is indexed for optimal query performance.</p>
     * 
     * @param email the email address to search for (case-sensitive)
     * @return an Optional containing the user if found, or empty if not found
     * @throws IllegalArgumentException if email is null
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given email address exists in the database.
     * 
     * <p>This method is used during registration to prevent duplicate email
     * addresses. It's more efficient than {@link #findByEmail(String)} when
     * only existence needs to be verified.</p>
     * 
     * @param email the email address to check (case-sensitive)
     * @return true if a user with the email exists, false otherwise
     * @throws IllegalArgumentException if email is null
     */
    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role.
     * 
     * <p>This method is primarily used for administrative purposes, such as
     * listing all providers or admins. The role field is indexed for optimal
     * query performance.</p>
     * 
     * <p>Note: This method returns all users with the specified role. For large
     * datasets, consider implementing pagination in the service layer.</p>
     * 
     * @param role the role to filter by
     * @return a list of users with the specified role (never null, empty if no matches)
     * @throws IllegalArgumentException if role is null
     */
    List<User> findByRole(Role role);
}
