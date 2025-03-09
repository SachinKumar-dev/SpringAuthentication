package com.authBackendSpring.springAuth.exceptionHandler;

//will be send to user as response upon exception using ResponseEntity class
public class ErrorResponse {    
    private final int StatusCode;
    private final String path;
    private final String errorMessage;
    private final long timeStamp;

    //const.
    public ErrorResponse(int StatusCode,String errorMessage,String path){
        this.StatusCode=StatusCode;
        this.errorMessage=errorMessage;
        this.path=path;
        this.timeStamp=System.currentTimeMillis();
    }


    //getters ,as JACKSON needs to acccess the fields in order to convert it into JSON format

    public int getStatusCode() {
        return StatusCode;
    }

    public String getPath() {
        return path;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    //setters are optional as we won't changing the field values directly
}
