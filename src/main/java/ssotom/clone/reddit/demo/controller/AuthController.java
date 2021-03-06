package ssotom.clone.reddit.demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ssotom.clone.reddit.demo.dto.request.RefreshTokenRequest;
import ssotom.clone.reddit.demo.exception.NotFoundException;
import ssotom.clone.reddit.demo.dto.request.LoginRequest;
import ssotom.clone.reddit.demo.dto.request.SingUpRequest;
import ssotom.clone.reddit.demo.dto.response.AuthenticationResponse;
import ssotom.clone.reddit.demo.dto.response.ErrorResponse;
import ssotom.clone.reddit.demo.dto.response.MessageResponse;
import ssotom.clone.reddit.demo.exception.SpringRedditException;
import ssotom.clone.reddit.demo.service.AuthService;
import ssotom.clone.reddit.demo.service.RefreshTokenService;

import javax.validation.Valid;

@AllArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SingUpRequest singUpRequest, BindingResult result) {
        validateRegisterRequest(singUpRequest, result);
        if(result.hasErrors()) {
            return ErrorResponse.returnError(result);
        }
        try {
            authService.signup(singUpRequest);
            return new ResponseEntity<>(new MessageResponse("Account created successfully, " +
                    "please check your inbox to validated your E-mail address"), HttpStatus.CREATED);
        } catch (DataAccessException e) {
            return ErrorResponse.returnError(e.getMostSpecificCause().getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
        if(result.hasErrors()) {
            return ErrorResponse.returnError(result);
        }
        try {
            AuthenticationResponse authenticationResponse = authService.login(loginRequest);
            return new ResponseEntity<>(authenticationResponse, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return ErrorResponse.returnError("Bad Credentials", HttpStatus.BAD_REQUEST);
        } catch(DisabledException e) {
            return ErrorResponse.returnError("Disabled Account", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/account-verification/{token}")
    public ResponseEntity<?> verifyAccount(@PathVariable String token) {
        try {
            authService.verifyAccount(token);
            return new ResponseEntity<>(new MessageResponse("Account activated successfully, now you can Log In"), HttpStatus.OK);
        } catch (NotFoundException e) {
            return ErrorResponse.returnError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest, BindingResult result) {
        if(result.hasErrors()) {
            return ErrorResponse.returnError(result);
        }
        try {
            return new ResponseEntity<>(authService.refreshToken(refreshTokenRequest), HttpStatus.OK);
        } catch (SpringRedditException e) {
            return ErrorResponse.returnError(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest, BindingResult result) {
        if(result.hasErrors()) {
            return ErrorResponse.returnError(result);
        }
        refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
        return new ResponseEntity<>(new MessageResponse("Refresh Token Deleted Successfully!"), HttpStatus.OK);
    }

    private void validateRegisterRequest(SingUpRequest singUpRequest, BindingResult result) {
        if(authService.existsByEmail(singUpRequest.getEmail())) {
            FieldError error = new FieldError("user", "email", singUpRequest.getEmail() + " in use");
            result.addError(error);
        }
        if(authService.existsByUsername(singUpRequest.getUsername())) {
            FieldError error = new FieldError("user", "username", singUpRequest.getUsername() + " in use");
            result.addError(error);
        }
    }

}
