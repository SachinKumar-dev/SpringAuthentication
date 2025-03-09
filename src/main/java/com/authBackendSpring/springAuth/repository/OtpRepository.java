package com.authBackendSpring.springAuth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.authBackendSpring.springAuth.models.Otp;

public interface OtpRepository extends MongoRepository<Otp, String> {

   public Optional<Otp> findByEmail(String email);

   public void deleteByEmail(String email);
    
}
