package com.service;

import com.entity.UserEntity;
import com.exception.ResourceConflictException;
import com.repository.UserRepository;
import com.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity getUserByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username,email)
                .orElse(null);
    }

    @Override
    public UserEntity addUser(UserEntity userEntity) {
        return userRepository.saveAndFlush(userEntity);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
