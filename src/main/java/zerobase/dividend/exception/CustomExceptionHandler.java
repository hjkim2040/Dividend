package zerobase.dividend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(AbstractException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(e.getStatusCode())
                .message(e.getMessage())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(e.getStatusCode()));
    }
}
