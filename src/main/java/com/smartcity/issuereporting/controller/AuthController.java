package com.smartcity.issuereporting.controller;

import com.smartcity.issuereporting.dto.Dtos.*;
import com.smartcity.issuereporting.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final HttpSessionSecurityContextRepository contextRepo = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, AuthenticationManager authManager) {
        this.userService = userService;
        this.authManager = authManager;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(r));
    }

    @PostMapping("/login")
    public UserDto login(@Valid @RequestBody LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
        Authentication auth;
        try {
            auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(r.email().trim().toLowerCase(), r.password()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account has been disabled");
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        req.getSession(true);
        contextRepo.saveContext(ctx, req, res);
        return userService.me(auth.getName());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        SecurityContextHolder.clearContext();
        return Map.of("message", "Logged out");
    }

    /** Returns the logged-in user, or 401 so the UI can redirect to the login page. */
    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return userService.me(auth.getName());
    }
}
