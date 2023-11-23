package com.example.tsgpaymentsystem.sprecifications;

import com.example.tsgpaymentsystem.domain.Payment;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.PaymentSearchCriteria;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PaymentSpecs {
    public static Specification<Payment> accordingToReportProperties(User user, PaymentSearchCriteria criteria) {
        return (root, criteriaQuery, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user"), user.getId()));

            if (criteria.getAccount() != null)
                predicates.add(cb.equal(root.get("account"), criteria.getAccount()));

            if (criteria.getService() != null)
                predicates.add(cb.equal(root.get("service"), criteria.getService()));


            if (criteria.getFrom() != null && criteria.getTo() != null)
                predicates.add(cb.between(root.get("timestamp"), criteria.getFrom(), criteria.getTo().plusDays(1).minusNanos(100)));


            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
