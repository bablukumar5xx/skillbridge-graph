package com.skillbridge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DatabaseUnavailableException.class)
    public Object handleDatabaseUnavailable(DatabaseUnavailableException ex,
                                            org.springframework.web.context.request.WebRequest request) {
        String path = request.getDescription(false).replace("uri=", "");
        if (path.startsWith("/api")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Database unavailable",
                            "message", "Unable to connect to CognoDB. Please check your connection settings."
                    ));
        }
        ModelAndView model = new ModelAndView("error");
        model.addObject("title", "Database Unavailable");
        model.addObject("message", ex.getMessage());
        return model;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid request", "message", ex.getMessage()));
    }
}
