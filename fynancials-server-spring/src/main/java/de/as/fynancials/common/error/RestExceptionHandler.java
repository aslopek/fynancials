package de.as.fynancials.common.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class RestExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Void> handleConstraintValidationException() {
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Void> handleBadRequestException() {
    return ResponseEntity.badRequest().build();
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Void> handleConflictException() {
    return ResponseEntity.status(HttpStatus.CONFLICT).build();
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<Void> handleInternalServerErrorException() {
    return ResponseEntity.internalServerError().build();
  }

  @ExceptionHandler(NoContentException.class)
  public ResponseEntity<Void> handleNoContentException() {
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Void> handleNotFoundException() {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(UnprocessableEntityException.class)
  public ResponseEntity<Void> handleUnprocessableEntityException() {
    return ResponseEntity.unprocessableEntity().build();
  }
}
