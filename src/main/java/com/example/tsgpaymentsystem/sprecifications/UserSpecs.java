package com.example.tsgpaymentsystem.sprecifications;

import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.UserSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class UserSpecs {

    public static Specification<User> accordingToReportProperties(UserSearchCriteria criteria) {
        return (root, criteriaQuery, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (!ObjectUtils.isEmpty(criteria.getEmail()))
                predicates.add(cb.equal(root.get("username"), criteria.getEmail()));

            if (!ObjectUtils.isEmpty(criteria.getPhone()))
                predicates.add(cb.equal(root.get("phone"), criteria.getPhone()));

            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
