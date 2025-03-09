package com.authBackendSpring.springAuth.controllers.apiControllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authBackendSpring.springAuth.configuration.JwtFilter;
import com.authBackendSpring.springAuth.exceptionHandler.CustomException;
import com.authBackendSpring.springAuth.models.Otp;
import com.authBackendSpring.springAuth.models.Users;
import com.authBackendSpring.springAuth.responseHandler.ResponseEntityHandler;
import com.authBackendSpring.springAuth.responseHandler.ResponseHanlder;
import com.authBackendSpring.springAuth.serviceLayer.EmailService.EmailService;
import com.authBackendSpring.springAuth.serviceLayer.UsersService.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    
  @Autowired
  private EmailService emailService;
  
    private final UserService usersService;


    public UserController (UserService service){
      this.usersService=service;
    }

    //register the user
    @PostMapping("/register")
    public ResponseEntity<ResponseHanlder<Object>> registerUser(@RequestBody Users user){ 
      String email=user.getEmail();
       if(email==null){
        return ResponseEntityHandler.error(400, "Email is required");
      }
      boolean isExist=usersService.doesUserExistByEmail(email);
      if(isExist){
        return ResponseEntityHandler.error(400,"User already exists");
      } 

       try {
        Users createdUser=usersService.registerUser(user);
        System.out.println(createdUser);
        if(createdUser!=null){
            return ResponseEntityHandler.getResponse(200, createdUser, "Success");
        }
        return ResponseEntityHandler.error(400, "Unable to create the user!");
       } catch (Exception e) {
            throw new CustomException(500, e.getMessage());
       }
    }

    //login user
    @PostMapping("/login")
    public ResponseEntity<ResponseHanlder<Object>> loginUser(@RequestBody Users user) {
      if(user.getEmail()==null || user.getPassword()==null){
        return ResponseEntityHandler.error(404, "Email or password missing");
      }
    Users loggedInUser = usersService.loginUser(user.getEmail(), user.getPassword());

    return switch (loggedInUser) {
        case null -> ResponseEntityHandler.error(404, "Warning, User not found!");
        case Users u when u.getId() == null -> ResponseEntityHandler.error(401, "Warning, Incorrect password!");
        default -> ResponseEntityHandler.getResponse(200, loggedInUser, "Logged In Successfully");
    };
}  


    //forgot pass-> need jwt verification
    @PostMapping("/resetPassword")
    public ResponseEntity<ResponseHanlder<Users>> resetPassword(@RequestBody Users user){
      String email=user.getEmail();
      String password=user.getPassword();
     try {
      if(email==null || password==null){
        return  ResponseEntityHandler.error(404, "Email or password missing");
      }
       boolean updated=usersService.forgotPass(email, password);
       if(updated){
         return ResponseEntityHandler.success("Password updated successfully!");
       }
       return ResponseEntityHandler.error(404,  "User not found!");
     } catch (Exception e) {
      throw new CustomException(500, e.getMessage());
     }
    }

    //delete account
    @PostMapping("/deleteUser")
    public ResponseEntity<ResponseHanlder<Object>> deleteUserAccount(){
      // String email=user.getEmail();
      try {
        //need to extract the email from token itself, get email from subject
       String email= JwtFilter.userEmail;
       System.out.println(email);
       int foundValue=usersService.deleteUserAccount(email);
       if(foundValue==200){
        return ResponseEntityHandler.success("User account has been deleted successfully!");
       }
       return ResponseEntityHandler.error(404, "No user found!");
      } catch (Exception e) {
        throw new CustomException(500, e.getMessage());
      }
    }

   //send otp
   @PostMapping("/sendOtp")
   public ResponseEntity<ResponseHanlder<Object>> sendOtp(@RequestBody Otp otp) {
       String email = otp.getEmail();
       if (email == null || email.isEmpty()) {
           return ResponseEntityHandler.error(404,"Email is required.");
       }
       emailService.sendAndSaveOtpToDb(email);
       return ResponseEntityHandler.success("OTP sent successfully to " + email);
   }

    //resend otp -> make sure to resend after 2 mins(expired time of sent otp)
    @PostMapping("/resendOtp")
    public ResponseEntity<ResponseHanlder<Object>> resendOtp(@RequestBody Otp otp) {
        String email = otp.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntityHandler.error(404,"Email is required.");
        }
        int status = emailService.resendOtp(email);
        switch (status) {
            case 200:
                return ResponseEntityHandler.success("OTP resent successfully to " + email);
            case 404:
                return ResponseEntityHandler.error(404,"Email not found.");
            default:
                return ResponseEntityHandler.error(500,"An unknown error occurred.");
        }
    }

    // API to Verify OTP
    @PostMapping("/verifyOtp")
    public ResponseEntity<ResponseHanlder<Object>> verifyOtp(@RequestBody Otp otp) {
        String email = otp.getEmail();
        String otpValue = otp.getOtp();
        try {
          if (email == null || email.isEmpty() || otpValue == null || otpValue.isEmpty()) {
              return ResponseEntityHandler.error(404,"Email and OTP are required.");
          }
          int status = emailService.validateOtp(email, otpValue);
          switch (status) {
            case 200:
              return ResponseEntityHandler.success("OTP verified successfully!");
            case 401:
              return ResponseEntityHandler.error(401, "OTP expired. Please resend OTP.");
            case 403:
              return ResponseEntityHandler.error(403, "Invalid OTP. Please try again.");
              case 404:
              return ResponseEntityHandler.error(404, "OTP not found.");
            default:
              return ResponseEntityHandler.error(500, "An unknown error occurred.");
          }
        } catch (Exception e) {
          throw new CustomException(500, e.getMessage());
        }
    }

    //logout  -> clear the token from frontend...
    @PostMapping("/logout")
    public ResponseEntity<ResponseHanlder<Object>> logoutUser(){
      String email= JwtFilter.userEmail;
      try {
        int status=usersService.logoutUser(email);
        if(status==200){
          return ResponseEntityHandler.success("User logout successfully!");
        }
        return ResponseEntityHandler.error(404,"No user found!");
      } catch (Exception e) {
        return ResponseEntityHandler.error(500,"Something went wrong!");
      }
    }


    //send otp during forgot password
    @PostMapping("/sendForgotPassOtp")
    public ResponseEntity<ResponseHanlder<Object>> sendForgotPassOtp(@RequestBody Otp otp) {
        String email = otp.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntityHandler.error(404,"Email is required.");
        }
        int status = emailService.sendForgotPassOtp(email);
        switch (status) {
            case 200:
                return ResponseEntityHandler.success("OTP sent successfully to " + email);
            case 404:
                return ResponseEntityHandler.error(404,"Email not found.");
            default:
                return ResponseEntityHandler.error(500,"An unknown error occurred.");
        }
    }

    //resend otp during forgot password -> make sure to send after 2 mins(expired time of sent otp)
    @PostMapping("/resendForgotPassOtp")
    public ResponseEntity<ResponseHanlder<Object>> resendForgotPassOtp(@RequestBody Otp otp) {
        String email = otp.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntityHandler.error(404,"Email is required.");
        }
        int status = emailService.resendForgotPassOtp(email);
        switch (status) {
            case 200:
                return ResponseEntityHandler.success("OTP sent successfully to " + email);
            case 404:
                return ResponseEntityHandler.success("Email not found.");
            default:
                return ResponseEntityHandler.error(500,"An unknown error occurred.");
        }
    }

    
}
