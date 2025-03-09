package com.authBackendSpring.springAuth.models;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection="otp")
@JsonIgnoreProperties("_class") // Ignore
@TypeAlias("Otp")
public class Otp {
        private String Id;
        private String email;
        private String createdAt;   
        private String otp;
        private String updatedAt;

        public Otp() {
        }

        public Otp(String email, String otp, String createdAt, String updatedAt) {
            this.email = email;
            this.otp = otp;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getOtp() {
            return otp;
        }


        public String getCreatedAt() {
            return createdAt;
        }


        public String getUpdatedAt() {
            return updatedAt;
        }

        public String getId() {
            return Id;
        }
}
