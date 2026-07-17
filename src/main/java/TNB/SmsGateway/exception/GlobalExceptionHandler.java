package TNB.SmsGateway.exception;
import TNB.SmsGateway.exception.api.ApiKeyInvalidException;
import TNB.SmsGateway.exception.api.ApiKeyRevokedException;
import TNB.SmsGateway.exception.api.InsufficientScopeException;
import TNB.SmsGateway.exception.api.MissingApiKeyException;
import TNB.SmsGateway.exception.authentication.InvalidOtpException;
import TNB.SmsGateway.exception.authentication.InvalidTokenException;
import TNB.SmsGateway.exception.authentication.OtpExpiredException;
import TNB.SmsGateway.exception.authentication.OtpRateLimitException;
import TNB.SmsGateway.exception.device.DeviceNotFoundException;
import TNB.SmsGateway.exception.device.DeviceNotOnlineException;
import TNB.SmsGateway.exception.device.DeviceSimException;
import TNB.SmsGateway.exception.device.InvalidPairingCodeException;
import TNB.SmsGateway.exception.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== BUSINESS EXCEPTIONS ====================

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getStatusCode())
                .error(ex.getErrorCode())
                .message(ex.getMessage())
                .path(getPath(request))
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    // ==================== AUTHENTICATION ====================

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOtp(InvalidOtpException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_OTP", ex.getMessage(), request);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(OtpExpiredException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", ex.getMessage(), request);
    }

    @ExceptionHandler(OtpRateLimitException.class)
    public ResponseEntity<ErrorResponse> handleOtpRateLimit(OtpRateLimitException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_OTP_REQUESTS", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", ex.getMessage(), request);
    }

    // ==================== API KEY ====================

    @ExceptionHandler(MissingApiKeyException.class)
    public ResponseEntity<ErrorResponse> handleMissingApiKey(MissingApiKeyException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "MISSING_API_KEY", ex.getMessage(), request);
    }

    @ExceptionHandler(ApiKeyInvalidException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyInvalid(ApiKeyInvalidException ex, WebRequest request) {
        String error = ex.getMessage().contains("Format") ? "INVALID_API_KEY_FORMAT" : "INVALID_API_KEY";
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, error, ex.getMessage(), request);
    }

    @ExceptionHandler(ApiKeyRevokedException.class)
    public ResponseEntity<ErrorResponse> handleApiKeyRevoked(ApiKeyRevokedException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "API_KEY_REVOKED", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientScopeException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientScope(InsufficientScopeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "INSUFFICIENT_SCOPE", ex.getMessage(), request);
    }

    // ==================== DEVICE ====================

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotFound(DeviceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DeviceNotOnlineException.class)
    public ResponseEntity<ErrorResponse> handleDeviceNotOnline(DeviceNotOnlineException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "DEVICE_NOT_ONLINE", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPairingCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPairingCode(InvalidPairingCodeException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_PAIRING_CODE", ex.getMessage(), request);
    }

    @ExceptionHandler(DeviceSimException.class)
    public ResponseEntity<ErrorResponse> handleDeviceSim(DeviceSimException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "DEVICE_SIM_ERROR", ex.getMessage(), request);
    }

    // ==================== MESSAGE ====================

    @ExceptionHandler(NoDeviceForCountryOperatorException.class)
    public ResponseEntity<ErrorResponse> handleNoDevice(NoDeviceForCountryOperatorException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "NO_DEVICE_FOR_COUNTRY_OPERATOR",
                ex.getMessage(), request);
    }

    @ExceptionHandler(OperatorCountryMismatchException.class)
    public ResponseEntity<ErrorResponse> handleOperatorCountryMismatch(OperatorCountryMismatchException ex,
                                                                       WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "OPERATOR_COUNTRY_MISMATCH", ex.getMessage(), request);
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotFound(MessageNotFoundException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "MESSAGE_NOT_FOUND", ex.getMessage(), request);
    }

    // ==================== WEBHOOK ====================

    @ExceptionHandler(WebhookException.class)
    public ResponseEntity<ErrorResponse> handleWebhook(WebhookException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "WEBHOOK_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(WebhookDeliveryException.class)
    public ResponseEntity<ErrorResponse> handleWebhookDelivery(WebhookDeliveryException ex, WebRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "WEBHOOK_DELIVERY_FAILED", ex.getMessage(), request);
    }

    // ==================== VALIDATION ====================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                    WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Erreur de validation des champs")
                .path(getPath(request))
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // ==================== GENERIC EXCEPTIONS ====================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_ERROR")
                .message("Une erreur interne est survenue")
                .path(getPath(request))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ==================== HELPERS ====================

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error,
                                                             String message, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(getPath(request))
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}