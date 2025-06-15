package com.example.demo.src.audit;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import java.util.Optional;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Repository
public class AuditDataManager {

    private final UserRepository userRepository;

    public Optional<User> getUserByIdAndState(Long userId, State state) {
        return userRepository.findUserByIdAndState(userId, state);
    }
}
