package com.example.tsgpaymentsystem.dto.seacrhcriteria;

import com.example.tsgpaymentsystem.dto.DateRange;
import com.example.tsgpaymentsystem.utils.DateUtils;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;

@Data
@Slf4j
@ToString
public class CalculationSearchCriteria {
    private Long service;
    private Long account;
    private String address;
    private String[] range;

    private int page = 0;
    private int count = 50;
    private ZonedDateTime from;
    private ZonedDateTime to;

    private Sort.Direction direction = Sort.Direction.ASC;
    private String sortProperty = "id";

    public Pageable getPageable() {
        Sort sort = Sort.by(new Sort.Order(getDirection(), getSortProperty()));
        return PageRequest.of(getPage(), getCount(), sort);
    }

    public void validate() {
        DateRange dateRange = DateUtils.parseRange(range);
        if (dateRange != null) {
            from = dateRange.getFrom();
            to = dateRange.getTo();

            ZonedDateTime now = DateUtils.todayStart();
            if (from == null && to != null) {
                if (to.isBefore(now))
                    from = now;
                else {
                    from = to;
                    to = now;
                }

            } else if (to == null && from != null) {
                if (from.isBefore(now))
                    to = now;
                else {
                    to = from;
                    from = now;
                }
            }

        }
    }
}
