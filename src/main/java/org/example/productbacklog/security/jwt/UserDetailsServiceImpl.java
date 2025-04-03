package org.example.productbacklog.security.jwt;

import org.example.productbacklog.entity.User;
import org.example.productbacklog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // Try to find user by email first
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseGet(() -> {
                    // If not found by email, try by username
                    return userRepository.findByUsername(usernameOrEmail)
                            .orElseThrow(() -> new UsernameNotFoundException(
                                    "User not found with email or username: " + usernameOrEmail));
                });

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}