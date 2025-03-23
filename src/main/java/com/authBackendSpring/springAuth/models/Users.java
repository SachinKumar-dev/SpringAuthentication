package com.authBackendSpring.springAuth.models;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

//specify the collection name
@Document(collection="users")
@JsonIgnoreProperties("_class") // Ignore when returning JSON
@TypeAlias("user")
public class Users {

    @Id
    private ObjectId _id;

    //go with other fields that may require
    @JsonInclude(JsonInclude.Include.NON_NULL) 
    private  String name;
    @JsonInclude(JsonInclude.Include.NON_NULL) 
    private  String mobileNumber;
    private  String email;
    
    @JsonInclude(JsonInclude.Include.NON_NULL) 
    private String otp;

    private String refreshToken;

    // Only include in JSON if not null
    @JsonInclude(JsonInclude.Include.NON_NULL) 
    private String accessToken;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  String password;


    //no-arg constructor
    public Users(){}

    //param. constructor
    public Users(String name, String mobileNumber, String email, String password) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.password = password;
    }

    public Users( String email, String password) {
        this.email = email;
        this.password = password;
    }

     public String getId() {
        return _id != null ? _id.toHexString() : null;
    }

    public void setId(String id) {
        this._id = new ObjectId(id);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    
}
