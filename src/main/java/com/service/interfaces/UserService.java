package com.service.interfaces;

import com.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    UserEntity getUserByUsernameOrEmail(String username,String email);
    UserEntity addUser(UserEntity userEntity);
}
