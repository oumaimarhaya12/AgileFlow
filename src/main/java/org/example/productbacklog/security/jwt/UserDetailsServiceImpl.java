package org.example.productbacklog.security.jwt;

import org.example.productbacklog.converter.UserConverter;
import org.example.productbacklog.dto.UserDTO;
import org.example.productbacklog.entity.User;
import org.example.productbacklog.repository.UserRepository;
import org.example.productbacklog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    @Lazy // Add @Lazy here to break the circular dependency
    private UserService userService;

    @Autowired
    private UserConverter userConverter;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by email first
        Optional<UserDTO> userDTO = userService.getUserByEmail(usernameOrEmail);

        if (!userDTO.isPresent()) {
            // If not found by email, try by username
            userDTO = userService.getUserByUsername(usernameOrEmail);

            if (!userDTO.isPresent()) {
                throw new UsernameNotFoundException("User not found with email or username: " + usernameOrEmail);
            }
        }

        // Convert DTO to entity to get the password
        User user = userConverter.convertToEntity(userDTO.get());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}