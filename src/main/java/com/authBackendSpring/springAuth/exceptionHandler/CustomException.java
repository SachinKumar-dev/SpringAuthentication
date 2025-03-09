package com.authBackendSpring.springAuth.exceptionHandler;

//this will use to throw the exception
public class CustomException extends RuntimeException{
    //status code holds
    private final int statusCode;
    //error message
    private final String errorMessage;

    public CustomException(int StatusCode,String errorMessage){
        this.statusCode=StatusCode;
        this.errorMessage=errorMessage;
    }

    //getters to access the private field values
    public int getStatusCode(){
        return statusCode;
    }

    public String getErrorMessage(){
        return errorMessage;
    }
}
