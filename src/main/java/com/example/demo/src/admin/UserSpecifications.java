package com.example.demo.src.admin;

import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.src.user.entity.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> containsName(String name) {
        return (root, query, criteriaBuilder) ->
            name == null || name.trim().isEmpty() ?
                criteriaBuilder.conjunction()
                : criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<User> hasId(Long id) {
        return (root, query, criteriaBuilder) ->
            id == null ? criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<User> hasState(State state) {
        return (root, query, criteriaBuilder) ->
            state == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.equal(root.get("state"), state);
    }


    public static Specification<User> createdOnDate(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            LocalDate dateOnly = date.toLocalDate();
            LocalDateTime startOfDay = dateOnly.atStartOfDay();
            LocalDateTime endOfDay = dateOnly.atTime(23, 59, 59, 999_999_999);

            return criteriaBuilder.between(root.get("createdAt"), startOfDay, endOfDay);
        };
    }

    public static Specification<User> stateIsNot(State state) {
        return (root, query, criteriaBuilder) ->
            state == null ? criteriaBuilder.conjunction()
                : criteriaBuilder.notEqual(root.get("state"), state);
    }

}
