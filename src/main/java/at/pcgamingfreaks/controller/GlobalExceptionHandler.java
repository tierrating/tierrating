package at.pcgamingfreaks.controller;

import at.pcgamingfreaks.model.dto.ErrorResponseDTO;
import at.pcgamingfreaks.model.exceptions.ThirdPartyAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnknownException(Exception e) {
        log.info("", e);
        return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.info("User '{}' not found", ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponseDTO("User not found"));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO("Invalid credentials"));
    }

    @ExceptionHandler(ThirdPartyAuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleThirdPartyAuthenticationException(ThirdPartyAuthenticationException ex) {
        return ResponseEntity.internalServerError().body(new ErrorResponseDTO(""));
    }
}
