package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.Account;
import com.example.tsgpaymentsystem.domain.Calculation;
import com.example.tsgpaymentsystem.domain.UploadData;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.dto.CalculationDto;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.CalculationSearchCriteria;
import com.example.tsgpaymentsystem.exception.FileContainsCorruptedLines;
import com.example.tsgpaymentsystem.repository.CalculationRepo;
import com.example.tsgpaymentsystem.repository.FileRepository;
import com.example.tsgpaymentsystem.sprecifications.CalculationSpecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class CalculationService {

    private final FileRepository fileRepository;
    private final CalculationRepo calculationsRepository;

    public CalculationService(FileRepository fileRepository, CalculationRepo calculationsRepository) {
        this.fileRepository = fileRepository;
        this.calculationsRepository = calculationsRepository;
    }

    public Calculation findLatestCalculationForServiceByAccount(Account account, com.example.tsgpaymentsystem.domain.Service service, Long lastUpload) {
        if (account == null)
            throw new IllegalArgumentException("Аккаунт не должен быть нулем");

        if (service == null)
            throw new IllegalArgumentException("Сервис тоже не может быть нулем");

        List<Calculation> calcs = calculationsRepository
                .findByAccountAndServiceAndLastUploadOrderById(account, service, lastUpload);

        log.debug(">>> Последний рассчет по сервису аккаунта найден {}", calcs);

        return calcs == null || calcs.isEmpty() ? null : calcs.get(0);
    }
    public void processCalculationsById(Long id, Long serviceId) throws ExecutionException, InterruptedException, TimeoutException, FileContainsCorruptedLines, FileNotFoundException {
        Optional<UploadData> uploadData = fileRepository.findById(id);
        if (uploadData.isEmpty())
            throw new FileNotFoundException("Нет рассчитываемого файла=" + id);

        UploadData calculationData = uploadData.get();

    }
    public Calculation update(Calculation calculation) {
        return calculationsRepository.save(calculation);
    }

    public Page<CalculationDto> findCurrentList(User user, Pageable pageable) {
        return calculationsRepository
                .findByUserAndLastUpload(user, user.getLastUpload(), pageable)
                .map(CalculationDto::of);
    }

    public Page<CalculationDto> findArchiveList(User user, CalculationSearchCriteria criteria) {

        return calculationsRepository
                .findAll(CalculationSpecs.accordingToReportProperties(user, criteria, user.getLastUploadDate()), criteria.getPageable())
                .map(CalculationDto::of);
    }

}
