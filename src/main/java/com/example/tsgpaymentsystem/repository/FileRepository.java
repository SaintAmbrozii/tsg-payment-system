package com.example.tsgpaymentsystem.repository;

import com.example.tsgpaymentsystem.domain.UploadData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<UploadData,Long>, JpaSpecificationExecutor<UploadData> {

}
