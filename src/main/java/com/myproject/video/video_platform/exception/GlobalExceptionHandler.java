package com.myproject.video.video_platform.exception;

import com.myproject.video.video_platform.dto.authetication.ErrorResponse;
import com.myproject.video.video_platform.dto.authetication.ValidationErrorResponse;
import com.myproject.video.video_platform.exception.auth.AuthenticationException;
import com.myproject.video.video_platform.exception.auth.CsrfException;
import com.myproject.video.video_platform.exception.auth.InvalidTokenException;
import com.myproject.video.video_platform.exception.auth.TokenExpiredException;
import com.myproject.video.video_platform.exception.auth.UserAlreadyExistingException;
import com.myproject.video.video_platform.exception.commerce.CommerceException;
import com.myproject.video.video_platform.exception.email.EmailSendingException;
import com.myproject.video.video_platform.exception.product.InvalidProductTypeException;
import com.myproject.video.video_platform.exception.product.PaymentRequiredException;
import com.myproject.video.video_platform.exception.product.ProductPublicationValidationException;
import com.myproject.video.video_platform.exception.product.ProductMediaException;
import com.myproject.video.video_platform.exception.product.QuizValidationException;
import com.myproject.video.video_platform.exception.product.ResourceNotFoundException;
import com.myproject.video.video_platform.exception.product.UnsupportedProductOperationException;
import com.myproject.video.video_platform.exception.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommerceException.class)
    public ResponseEntity<ErrorResponse> handleCommerceException(CommerceException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        // Collect all field errors
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        // Build a list or map of field -> error messages
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ValidationErrorResponse response = new ValidationErrorResponse(
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CsrfException.class)
    public ResponseEntity<ErrorResponse> handleCsrfException(CsrfException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse error = new ErrorResponse("Access Denied", HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserAlreadyExistingException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExistingException(UserAlreadyExistingException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidProductTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductTypeException(InvalidProductTypeException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(QuizValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleQuizValidationException(QuizValidationException ex) {
        ValidationErrorResponse response = new ValidationErrorResponse(
                ex.getMessage(),
                ex.getErrors()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductPublicationValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handlePublicationValidation(
            ProductPublicationValidationException ex
    ) {
        return new ResponseEntity<>(
                new ValidationErrorResponse(ex.getMessage(), ex.getErrors()),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(ProductMediaException.class)
    public ResponseEntity<ErrorResponse> handleProductMediaException(ProductMediaException ex) {
        return new ResponseEntity<>(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedProductOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedProductOperationException(
            UnsupportedProductOperationException ex
    ) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PaymentRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePaymentRequiredException(PaymentRequiredException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage(), HttpStatus.PAYMENT_REQUIRED.value());
        return new ResponseEntity<>(error, HttpStatus.PAYMENT_REQUIRED);
    }
}
