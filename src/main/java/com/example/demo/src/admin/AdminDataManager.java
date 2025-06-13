package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Repository
public class AdminDataManager {

    private final UserRepository userRepository;

    public Page<User> getAllUserInfos(Specification<User> spec, Pageable pageable) {

        return userRepository.findAll(spec, pageable);
    }

    public Optional<User> getUserInfoById(Long id) {
        return userRepository.findUserById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteUserById(userId);
    }

    public Optional<User> getUserByIdAndState(Long id, State state) {
        return userRepository.findUserByIdAndState(id, state);
    }

    public Optional<User> getUserByNameAndState(String name, State state) {
        return userRepository.findUserByNameAndState(name, state);
    }
}
