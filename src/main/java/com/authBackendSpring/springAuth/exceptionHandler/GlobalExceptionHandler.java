package com.authBackendSpring.springAuth.exceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    //handle or catch the custom class's exception and return using Error Response class
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex,WebRequest request){
        ErrorResponse errorResponse=new ErrorResponse(ex.getStatusCode(), ex.getErrorMessage(), request.getContextPath());
        //here errorResponse is body need to send and Httpstatus is actual code for req.
        return new ResponseEntity<ErrorResponse>(errorResponse,HttpStatus.valueOf(ex.getStatusCode()));
    }


     // Handle all other exceptions
     //any exception like null pointer,arithmetic will be handled here just throw any exception from the controller
     @ExceptionHandler(Exception.class)
     public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
         ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage(),request.getContextPath());
         return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
     }


}
