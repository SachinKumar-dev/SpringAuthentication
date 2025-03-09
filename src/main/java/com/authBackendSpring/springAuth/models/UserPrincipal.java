package com.authBackendSpring.springAuth.models;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails{

    Users user;

    public UserPrincipal(Users user){
        this.user=user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
       return Collections.singleton((new SimpleGrantedAuthority("USER")));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    //as it will use email for authentication purpose
    @Override
    public String getUsername() {
       return user.getEmail();
    }

     // New method to get Name
     public String getName() {
        return user.getName();
    }
}
