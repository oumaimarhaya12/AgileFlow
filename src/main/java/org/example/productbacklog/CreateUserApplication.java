package org.example.productbacklog;

import org.example.productbacklog.entity.User;
import org.example.productbacklog.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class CreateUserApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(CreateUserApplication.class, args);

        // Get the UserService bean
        UserService userService = context.getBean(UserService.class);

        // Create and save a new user with password that will be encoded
        User newUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("securePassword123") // This will be encoded by the service
                .role(User.Role.DEVELOPER)
                .build();

        User savedUser = userService.createUser(newUser);

        // Print confirmation with encoded password
        System.out.println("User created successfully!");
        System.out.println("ID: " + savedUser.getId());
        System.out.println("Username: " + savedUser.getUsername());
        System.out.println("Email: " + savedUser.getEmail());
        System.out.println("Encoded Password: " + savedUser.getPassword());
        System.out.println("Role: " + savedUser.getRole());

        // Verify password encoding by testing authentication
        boolean authResult = userService.authenticateUser("testuser", "securePassword123").isPresent();
        System.out.println("Authentication test: " + (authResult ? "PASSED" : "FAILED"));

        // Test with wrong password (should fail)
        boolean failedAuth = userService.authenticateUser("testuser", "wrongPassword").isPresent();
        System.out.println("Authentication with wrong password test: " + (!failedAuth ? "PASSED" : "FAILED"));
    }

    /**
     * Alternative: CommandLineRunner approach if you prefer to run this within the application startup
     */
    @Bean
    public CommandLineRunner createUserRunner(UserService userService) {
        return args -> {
            // Check if command line argument is passed to run this script
            if (args.length > 0 && "create-test-user".equals(args[0])) {
                // Create and save a new user with password that will be encoded
                User newUser = User.builder()
                        .username("cmduser")
                        .email("cmd@example.com")
                        .password("cmdPassword456") // This will be encoded by the service
                        .role(User.Role.PRODUCT_OWNER)
                        .build();

                User savedUser = userService.createUser(newUser);

                // Print confirmation with encoded password
                System.out.println("Command line user created successfully!");
                System.out.println("ID: " + savedUser.getId());
                System.out.println("Username: " + savedUser.getUsername());
                System.out.println("Email: " + savedUser.getEmail());
                System.out.println("Encoded Password: " + savedUser.getPassword());
                System.out.println("Role: " + savedUser.getRole());
            }
        };
    }
}