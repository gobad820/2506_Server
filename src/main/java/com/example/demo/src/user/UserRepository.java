package com.example.demo.src.user;

import static com.example.demo.common.entity.BaseEntity.State;

import com.example.demo.src.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findUserByIdAndState(Long id, State state);

    Optional<User> findUserByEmailAndState(String email, State state);

    List<User> findAllByEmailAndState(String email, State state);

    List<User> findAllByState(State state);

    Optional<User> findUserById(Long id);

    void deleteUserById(Long id);

    Optional<User> findUserByNameAndState(String name, State state);

    Optional<User> findUserByEmail(String email);
}
