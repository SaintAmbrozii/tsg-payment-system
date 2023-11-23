package com.example.tsgpaymentsystem.dto.seacrhcriteria;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@ToString
@Slf4j
public class UserSearchCriteria {


    private String email;
    private String phone;

    private int page = 0;
    private int count = 50;

    private Sort.Direction direction = Sort.Direction.DESC;
    private String sortProperty = "id";


    public Pageable getPageable() {
        Sort sort = Sort.by(new Sort.Order(getDirection(), getSortProperty()));
        return PageRequest.of(getPage(), getCount(), sort);
    }


}
