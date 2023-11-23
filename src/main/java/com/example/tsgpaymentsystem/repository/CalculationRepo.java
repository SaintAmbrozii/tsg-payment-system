package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepo extends JpaRepository<Calculation,Long>, JpaSpecificationExecutor<Calculation> {

    List<Calculation> findByAccountAndServiceAndLastUploadOrderById(Account account, Service service, Long lastUploadId);

    List<Calculation> findByUserAndAccountAndAddressAndLastUploadOrderById(User user, Account account, Address address, Long lastUploadId);

    Page<Calculation> findByUserAndLastUpload(User user, Long lastUploadId, Pageable pageable);

    void deleteByLastUpload(Long uploadId);


}
