package com.authBackendSpring.springAuth.serviceLayer.UsersService;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.authBackendSpring.springAuth.models.Users;
import com.authBackendSpring.springAuth.repository.UserRepository;

@Service
public class UserService implements UserDetailsService{

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final UserRepository userRepository;
  private final JwtService jwtService;

  public UserService(UserRepository repository, JwtService service){
    this.userRepository = repository;
    this.jwtService = service;
  }

  // Bcrypt logic, encoder
  public String endcodePassword(String password){
    return passwordEncoder.encode(password);
  }

  // Decoder or matcher --> from db
  public boolean decodePassword(String rawPassword, String enocdedPassword){
    return passwordEncoder.matches(rawPassword, enocdedPassword);
  }

  // Encrypt the pass and save to db too
  public String encryptPass(String password){
    return passwordEncoder.encode(password);
  }

  // Check during login encrypted pass to given pass by user
  public boolean checkPassword(String rawPassword, String encodedPassword){
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  // DB operations
  
  // Register user
  public Users registerUser(Users user) {
    String encodedPassword = endcodePassword(user.getPassword());
    user.setPassword(encodedPassword);
    Users newUser = new Users(user.getEmail(), user.getPassword());
    return userRepository.save(newUser);
  }

  // Check if user already exists during registration
  public boolean doesUserExistByEmail(String email) {
    Optional<Users> userOptional = userRepository.findByEmail(email);
    return userOptional.isPresent(); 
  }

  // Login 
  public Users loginUser(String email, String rawPassword) { 
    Optional<Users> userOptional = userRepository.findByEmail(email);
  
    if (userOptional.isEmpty()) {
      return null; 
    }
  
    Users user = userOptional.get();
  
    if (!checkPassword(rawPassword, user.getPassword())) {
      return new Users(); 
    }
  
    // Generate tokens
    String refreshToken = jwtService.generateRefreshToken(user.getId());
    String accessToken = jwtService.generateAccessToken(email);
  
    // Save only the refresh token to DB
    user.setRefreshToken(refreshToken);
    userRepository.save(user);
  
    // Return user object but without saving access token in DB
    // Only for response, not persisted
    user.setAccessToken(accessToken); 
    return user;
  }
  
  // Forgot password
  public boolean forgotPass(String email, String password){
    Optional<Users> userOptional = userRepository.findByEmail(email);
    if(userOptional.isPresent()){
      String newPassword = endcodePassword(password);
      userRepository.updatePasswordByEmail(email, newPassword);
      return true;
    } else {
      return false;
    }
  }

  // Delete user account
  public int deleteUserAccount(String email){
    Optional<Users> userOptional = userRepository.findByEmail(email);
    if(userOptional.isPresent()){
      userRepository.deleteByEmail(email);
      return 200;
    }
    return 404;
  }

  // Log the user out
  public int logoutUser(String email) {
    Optional<Users> userOptional = userRepository.findByEmail(email);
    if(userOptional.isPresent()){
      return 200;
    }
    return 404;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    final Users user;
    Optional<Users> userOptional = userRepository.findByEmail(email);
    user = userOptional.get();
    try {
      if(user == null){
        throw new UsernameNotFoundException("User not found with given email id" + email);
      }
      return new com.authBackendSpring.springAuth.models.UserPrincipal(user);
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }
}
