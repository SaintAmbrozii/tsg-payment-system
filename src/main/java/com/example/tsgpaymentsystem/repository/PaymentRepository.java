package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long>, JpaSpecificationExecutor<Payment> {

    Page<Payment> findByUserAndTimestampGreaterThanEqualOrderByTimestampDesc(User user, ZonedDateTime lastTwoMonths, Pageable pageable);

    List<Payment> findByServiceAndAccountAndLastUpload(Service service, Account account, Long lastUpload);

    List<Payment> findByUserAndAccountAndAddressAndLastUploadOrderById(User user, Account account, Address address, Long lastUpload);
}
