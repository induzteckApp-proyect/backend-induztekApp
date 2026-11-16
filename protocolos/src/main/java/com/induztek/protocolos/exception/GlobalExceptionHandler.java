package com.induztek.protocolos.exception;
import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.HttpStatus; import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler; import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException; import java.time.Instant; import java.util.LinkedHashMap; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String,String> fields=new LinkedHashMap<>(); ex.getBindingResult().getFieldErrors().forEach(e->fields.putIfAbsent(e.getField(),e.getDefaultMessage()));
        return body(HttpStatus.BAD_REQUEST,"La solicitud contiene campos inválidos.",req,fields);
    }
    @ExceptionHandler(ConstraintViolationException.class) ResponseEntity<?> constraint(ConstraintViolationException ex,HttpServletRequest req){return body(HttpStatus.BAD_REQUEST,ex.getMessage(),req,Map.of());}
    @ExceptionHandler(ResponseStatusException.class) ResponseEntity<?> status(ResponseStatusException ex,HttpServletRequest req){return body(HttpStatus.valueOf(ex.getStatusCode().value()),ex.getReason(),req,Map.of());}
    @ExceptionHandler(IllegalArgumentException.class) ResponseEntity<?> argument(IllegalArgumentException ex,HttpServletRequest req){return body(HttpStatus.BAD_REQUEST,ex.getMessage(),req,Map.of());}
    @ExceptionHandler(IllegalStateException.class) ResponseEntity<?> state(IllegalStateException ex,HttpServletRequest req){return body(HttpStatus.CONFLICT,ex.getMessage(),req,Map.of());}
    @ExceptionHandler(AccessDeniedException.class) ResponseEntity<?> denied(AccessDeniedException ex,HttpServletRequest req){return body(HttpStatus.FORBIDDEN,"No tienes permisos para realizar esta operación.",req,Map.of());}
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> conflict(DataIntegrityViolationException ex,HttpServletRequest req){return body(HttpStatus.CONFLICT,"El registro entra en conflicto con datos existentes.",req,Map.of());}
    @ExceptionHandler(Exception.class) ResponseEntity<?> unexpected(Exception ex,HttpServletRequest req){return body(HttpStatus.INTERNAL_SERVER_ERROR,"Ocurrió un error interno.",req,Map.of());}
    private ResponseEntity<?> body(HttpStatus status,String message,HttpServletRequest req,Map<?,?> details){return ResponseEntity.status(status).body(Map.of("timestamp",Instant.now(),"status",status.value(),"error",status.getReasonPhrase(),"message",message==null?"Error":message,"path",req.getRequestURI(),"details",details));}
}
