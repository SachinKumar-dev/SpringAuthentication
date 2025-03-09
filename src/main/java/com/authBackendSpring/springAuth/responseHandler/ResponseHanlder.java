package com.authBackendSpring.springAuth.responseHandler;

public class ResponseHanlder<T> {
    //status code
    private final int statusCode;
    //data
    private final T data;
    //custom message
    private final String customMessage;
    

    public ResponseHanlder(int statusCode, T data, String customMessage) {
        this.statusCode = statusCode;
        this.data = data;
        this.customMessage = customMessage;
    }

    public ResponseHanlder(int statusCode, String customMessage) {
        this.statusCode = statusCode;
        this.data=null;
        this.customMessage = customMessage;
    }


    public int getStatusCode() {
        return statusCode;
    }



    public T getData() {
        return data;
    }



    public String getCustomMessage() {
        return customMessage;
    }


}
