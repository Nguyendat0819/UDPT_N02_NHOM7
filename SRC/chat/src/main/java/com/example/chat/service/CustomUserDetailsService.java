package com.example.chat.service;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        // tim user theo email
        User user = userRepository.findByEmail(email);
        if(user == null){
            throw new UsernameNotFoundException("Email khong ton tai: " + email);
        }
        return new org.springframework.security.core.userdetails.User(
           user.getEmail(),
           user.getPassword(),
           java.util.Collections.emptyList()
        );
    }
}
