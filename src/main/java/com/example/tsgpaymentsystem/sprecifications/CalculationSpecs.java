package com.example.tsgpaymentsystem.sprecifications;

import com.example.tsgpaymentsystem.domain.Calculation;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.CalculationSearchCriteria;
import com.example.tsgpaymentsystem.utils.DateUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;


import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;



public class CalculationSpecs {

    public static Specification<Calculation> accordingToReportProperties(User user, CalculationSearchCriteria criteria, ZonedDateTime lastUpload) {
        if (lastUpload == null)
            lastUpload = DateUtils.todayStart();

        ZonedDateTime finalLastUpload = lastUpload;
        return (root, criteriaQuery, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("user"), user.getId()));

            if (criteria.getAccount() != null)
                predicates.add(cb.equal(root.get("account"), criteria.getAccount()));

            if (criteria.getService() != null)
                predicates.add(cb.equal(root.get("service"), criteria.getService()));

            if (criteria.getFrom() != null && criteria.getTo() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lastUploadDate"), criteria.getFrom()));
                predicates.add(cb.lessThanOrEqualTo(root.get("lastUploadDate"), criteria.getTo()));
            } else
                predicates.add(cb.lessThan(root.get("lastUploadDate"), finalLastUpload));


            return cb.and(predicates.toArray(new Predicate[]{}));
        };
    }
}
