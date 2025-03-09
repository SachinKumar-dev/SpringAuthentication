package com.authBackendSpring.springAuth.responseHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseEntityHandler {

    // Utility method to create a custom response
public static ResponseEntity<ResponseHanlder<Object>> getResponse(int statusCode, Object data, String customMessage) {
    ResponseHanlder<Object> responseHandler = new ResponseHanlder<>(statusCode, data, customMessage);
    return new ResponseEntity<>(responseHandler, HttpStatus.valueOf(statusCode));
}


    public static <T> ResponseEntity<ResponseHanlder<T>> success(String message) {
        return ResponseEntity.ok(new ResponseHanlder<>(200, message));
    }

    public static <T> ResponseEntity<ResponseHanlder<T>> error(int status, String message) {
        return ResponseEntity.status(status).body(new ResponseHanlder<>(status, message));
    }
}