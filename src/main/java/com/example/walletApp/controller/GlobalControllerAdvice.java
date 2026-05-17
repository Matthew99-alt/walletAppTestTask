package com.example.walletApp.controller;

import com.example.walletApp.exception.InsufficientFundsException;
import com.example.walletApp.model.dto.ErrorDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * ControllerAdvice для корректного вывода сообщений об ошибках
 */

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorDTO handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(ex.getMessage());
        errorDTO.setNumber(HttpStatus.NOT_FOUND.value());
        errorDTO.setDescription(HttpStatus.NOT_FOUND.getReasonPhrase());

        return errorDTO;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDTO handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage("Invalid JSON format");
        errorDTO.setNumber(HttpStatus.BAD_REQUEST.value());
        errorDTO.setDescription(HttpStatus.BAD_REQUEST.getReasonPhrase());

        return errorDTO;
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDTO handleInsufficientFundsException(InsufficientFundsException ex){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(ex.getMessage());
        errorDTO.setNumber(HttpStatus.CONFLICT.value());
        errorDTO.setDescription(HttpStatus.CONFLICT.getReasonPhrase());

        return errorDTO;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorDTO handleRuntimeException(RuntimeException ex) {
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage(ex.getMessage());
        errorDTO.setNumber(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorDTO.setDescription(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());

        return errorDTO;
    }
}
