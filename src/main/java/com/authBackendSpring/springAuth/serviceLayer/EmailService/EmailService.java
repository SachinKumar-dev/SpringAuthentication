package com.authBackendSpring.springAuth.serviceLayer.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.authBackendSpring.springAuth.models.Users;
import com.authBackendSpring.springAuth.models.Otp;
import com.authBackendSpring.springAuth.repository.OtpRepository;
import com.authBackendSpring.springAuth.repository.UserRepository;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UserRepository userRepository;

    
    // Generate 4-digit OTP
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    // Send OTP via Email
    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp + ". It is valid for 2 minutes.");
        mailSender.send(message);
    }

    
//send otp -> save to db -> validate(given mail -> find otp from db and compare with given otp from userInput)
//save otp to db
public int sendAndSaveOtpToDb(String email) {
    String generatedOtp = generateOtp();
   try {
     sendOtp(email,generatedOtp);
     Otp newOtp = new Otp(email, generatedOtp, new Date().toString(), new Date().toString());
     otpRepository.save(newOtp);
     return 200;
   } catch (Exception e) {
    return 500;
   }
}

// Validate OTP
public int validateOtp(String email, String inputOtp) {
    Optional<Otp> otpOptional = otpRepository.findByEmail(email);
    if (!otpOptional.isPresent()) {
        //Otp not found
        return 404; 
    }

    Otp otp = otpOptional.get();

    //check ig otp is expired
   String otpCreationTime=otp.getCreatedAt();
   SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
   try {
     Date otpDate = dateFormat.parse(otpCreationTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(otpDate);
            LocalTime otpTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            LocalTime currentTime = LocalTime.now();

            Duration duration = Duration.between(otpTime, currentTime);
            long diffSeconds = duration.getSeconds();

            if (diffSeconds > 120) { 
                // OTP expired
                otpRepository.deleteByEmail(email);
                return 401;
            }
            if (otp.getOtp().equals(inputOtp)) {
                // OTP matched
                otpRepository.deleteByEmail(email);
                return 200;
            }
            else{
            // OTP mismatch 
            return 403; 
            }
   } catch (Exception e) {
    return 500;  
}
}

    //resend otp
    public int resendOtp(String email){
       int status= sendAndSaveOtpToDb(email);
         return status;
    }

    //sendOtp during forgot password
    public int sendForgotPassOtp(String email){
        try {
            Optional<Users> userOptional = userRepository.findByEmail(email);
           if(userOptional.isPresent()){
            String generatedOtp = generateOtp();
            sendOtp(email, generatedOtp);
            Otp newOtp = new Otp(email, generatedOtp, new Date().toString(), new Date().toString());
            otpRepository.save(newOtp);
            return 200;
           }
           else{
            return 404;
           }
        } catch (Exception e) {
            return 500;
        }
    }

    //resend otp during forgot password
    public int resendForgotPassOtp(String email){
      return sendForgotPassOtp(email);
    }
}