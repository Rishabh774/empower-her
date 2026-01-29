package com.empowerher.services;

import com.empowerher.entities.User;
import com.empowerher.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // EmailService dependency remove kiya hai - circular dependency avoid karne ke liye

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        try {
            // Encrypt password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            
            System.out.println("User created successfully: " + savedUser.getUsername());
            System.out.println("Email notifications will be sent via event listener");
            
            return savedUser;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw new RuntimeException("Error creating user: " + e.getMessage());
        }
    }

    public User updateUser(Long id, User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            
            // Only update password if provided
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            
            user.setRole(userDetails.getRole());
            user.setEnabled(userDetails.isEnabled());
            
            User updatedUser = userRepository.save(user);
            System.out.println("User updated successfully: " + updatedUser.getUsername());
            return updatedUser;
        }
        return null;
    }

    public void deleteUser(Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                userRepository.deleteById(id);
                System.out.println("User deleted successfully: ID " + id);
            } else {
                throw new RuntimeException("User not found with id: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public long countUsers() {
        return userRepository.count();
    }

    public User updateProfile(Long userId, String username, String email) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // Check if username already exists (excluding current user)
            if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists (excluding current user)
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already registered");
            }
            
            user.setUsername(username);
            user.setEmail(email);
            
            User updatedUser = userRepository.save(user);
            System.out.println("User profile updated successfully: " + updatedUser.getUsername());
            return updatedUser;
        }
        throw new RuntimeException("User not found with id: " + userId);
    }

    // Get active users count
    public long countActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::isEnabled)
                .count();
    }

    // Get users by role
    public List<User> getUsersByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .toList();
    }

    // Toggle user status
    public User toggleUserStatus(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setEnabled(!user.isEnabled());
            
            User updatedUser = userRepository.save(user);
            System.out.println("User status updated: " + updatedUser.getUsername() + " - Enabled: " + updatedUser.isEnabled());
            return updatedUser;
        }
        throw new RuntimeException("User not found with id: " + userId);
    }

    // Change user password
    public User changePassword(Long userId, String newPassword) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            
            User updatedUser = userRepository.save(user);
            System.out.println("Password changed for user: " + updatedUser.getUsername());
            return updatedUser;
        }
        throw new RuntimeException("User not found with id: " + userId);
    }

    // Search users by username or email
    public List<User> searchUsers(String query) {
        return userRepository.findAll().stream()
                .filter(user -> 
                    user.getUsername().toLowerCase().contains(query.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }

    // Get recent users
    public List<User> getRecentUsers(int limit) {
        return userRepository.findAll().stream()
                .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
                .limit(limit)
                .toList();
    }

    // Verify user credentials
    public boolean verifyCredentials(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    // Check if user exists and is enabled
    public boolean isUserActive(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        return optionalUser.isPresent() && optionalUser.get().isEnabled();
    }

    // Get user statistics
    public UserStatistics getUserStatistics() {
        List<User> allUsers = userRepository.findAll();
        
        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        long adminUsers = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long regularUsers = allUsers.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        
        return new UserStatistics(totalUsers, activeUsers, adminUsers, regularUsers);
    }

    // Inner class for user statistics
    public static class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long adminUsers;
        private final long regularUsers;

        public UserStatistics(long totalUsers, long activeUsers, long adminUsers, long regularUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.adminUsers = adminUsers;
            this.regularUsers = regularUsers;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getAdminUsers() { return adminUsers; }
        public long getRegularUsers() { return regularUsers; }

        @Override
        public String toString() {
            return String.format("UserStatistics{total=%d, active=%d, admins=%d, users=%d}", 
                totalUsers, activeUsers, adminUsers, regularUsers);
        }
    }
}