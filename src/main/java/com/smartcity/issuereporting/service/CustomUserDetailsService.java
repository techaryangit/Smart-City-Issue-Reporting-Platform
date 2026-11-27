package com.smartcity.issuereporting.service;

import com.smartcity.issuereporting.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.smartcity.issuereporting.model.User u = users.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No user with email " + email));
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(), u.getPassword(), u.isEnabled(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())));
    }
}
