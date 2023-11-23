package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.Account;

import com.example.tsgpaymentsystem.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    List<Account> findAllByUserOrderByAccount(User user);

    List<Account> findByUserAndAccountAndLastUpload(User user, String account, Long lastUploadId);

}
