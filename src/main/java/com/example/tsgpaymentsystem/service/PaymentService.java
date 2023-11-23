package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.*;
import com.example.tsgpaymentsystem.dto.PaymentDto;
import com.example.tsgpaymentsystem.dto.PaymentRecordDto;
import com.example.tsgpaymentsystem.dto.seacrhcriteria.PaymentSearchCriteria;
import com.example.tsgpaymentsystem.exception.FileNotFoundException;
import com.example.tsgpaymentsystem.exception.UserNotFoundException;
import com.example.tsgpaymentsystem.job.PaymentAppendJob;
import com.example.tsgpaymentsystem.repository.*;
import com.example.tsgpaymentsystem.sprecifications.PaymentSpecs;
import com.example.tsgpaymentsystem.utils.AddressRecord;
import com.example.tsgpaymentsystem.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private Scheduler scheduler;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final ServiceRepository serviceRepository;
    private final AddressRepository addressRepository;
    private final CalculationService calculationService;
    private final ReportService reportService;
    private final UserRepository userRepository;


    public PaymentService(PaymentRepository paymentRepository,
                          AccountRepository accountRepository, ServiceRepository serviceRepository,
                          AddressRepository addressRepository, CalculationService calculationService,
                          ReportService reportService, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.accountRepository = accountRepository;
        this.serviceRepository = serviceRepository;
        this.addressRepository = addressRepository;
        this.calculationService = calculationService;
        this.reportService = reportService;
        this.userRepository = userRepository;
    }
    public byte[] createExportCSVFile(User user, PaymentSearchCriteria searchCriteria) throws Exception {
        log.debug(">>> createExportCSVFile for {}", user);
        return reportService.composeCSV(user.getContract(), findPaymentsByCriteria(user, searchCriteria), !user.getIsAggregated());
    }

    public byte[] createExportXLSFile(User user, PaymentSearchCriteria searchCriteria) throws Exception {
        log.debug(">>> createExportXLSFile for {}", user);
        return reportService.composeXLS(user.getContract(), findPaymentsByCriteria(user, searchCriteria), !user.getIsAggregated());
    }

    public String createExportPrinted(User user, PaymentSearchCriteria searchCriteria) {
        List<Payment> paymentList = paymentRepository
                .findAll(PaymentSpecs.accordingToReportProperties(user, searchCriteria),
                        Sort.by(Sort.Direction.ASC, "timestamp"));

        return reportService.composePrintable(user.getContract(), paymentList, searchCriteria);
    }

    @Transactional
    public void recalculationOfDebts(Long paymentId) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);
        if (paymentOptional.isEmpty())
            throw new FileNotFoundException("Платеж для перерасчета не найден ID=" + paymentId);

        Calculation calculation = null;
        double debt = 0f;
        Payment payment = paymentOptional.get();
        com.example.tsgpaymentsystem.domain.Service service = payment.getService();
        Account account = payment.getAccount();

        List<Payment> paymentsAfterLastUploadedCalculations;

        calculation = calculationService.findLatestCalculationForServiceByAccount(account, service, account.getUser().getLastUpload());
        if (calculation == null) {
            //    throw new IllegalStateException("Начисления для платеж не найдены.");
            // Don't fail, track payments anyway
            log.error("Начисления для платеж не найдены. {}. В расчет попадут платежи за последний месяц по этой услуге", payment);
            debt = 0f;
        } else {
            debt = calculation.getDebt();
        }

        paymentsAfterLastUploadedCalculations =
                paymentRepository.findByServiceAndAccountAndLastUpload(service, account, account.getUser().getLastUpload());

        double alreadyPayed = paymentsAfterLastUploadedCalculations.stream()
                .map(Payment::getPayment)
                .reduce(0d, Double::sum);

        log.debug(">> Found already payed amount {} but debt is {}", alreadyPayed, debt);

        debt -= alreadyPayed;
        payment.setOutstandingDebt(debt);
        payment = paymentRepository.save(payment);

        if (calculation != null) {
            calculation.setOutstandingDebt(payment.getOutstandingDebt());
            calculation = calculationService.update(calculation);
        }

        log.debug(">> Recalculation for payment {} is done {}", payment, calculation);
    }

    public void addPayments(User user, PaymentRecordDto data) throws SchedulerException {
        for (PaymentRecordDto.Record r : data.getPayments())
            addPayment(user, r, data.getTsgId());
    }

    public void addPayment(User agent, PaymentRecordDto.Record data, String tsgId) throws SchedulerException {

        // TODO: add a check that the user belongs to the agent

        User user = userRepository.findByEmail(tsgId).orElseThrow();


        com.example.tsgpaymentsystem.domain.Service service = serviceRepository.findByUserAndService(user, data.getService());
        if (service == null) {
            throw new IllegalStateException("Запрашиваемая услуга '"
                    + data.getService()
                    + "' не найдена. ");

        }

        Address address = null;
        Account foundAccount = null;

        List<Account> accounts = accountRepository
                .findByUserAndAccountAndLastUpload(user, data.getAccount(), user.getLastUpload());
        if (accounts == null || accounts.isEmpty())
            throw new IllegalStateException("Аккаунт не найден. " + data.getAccount());

        if (accounts.size() > 1 && ObjectUtils.isEmpty(data.getAddress()))
            throw new IllegalStateException("Существует несколько аккаунтов, уточните адрес. " + data.getAccount());

        // нет адреса
        if (ObjectUtils.isEmpty(data.getAddress())) {
            foundAccount = accounts.get(0);
            List<Address> addresses = addressRepository.findByUserAndAccount(user, foundAccount);
            if (addresses != null && addresses.size() > 1)
                throw new IllegalStateException("К аккаунту "
                        + foundAccount
                        + " привязаны несколько адресов. ");

            if (addresses == null || addresses.isEmpty())
                throw new IllegalStateException("Адресов не найдено, аккант: " + foundAccount);

            address = addresses.get(0);

        } else {

            // есть адресс
            AddressRecord addressRecord = AddressRecord.createAddressRecord(data.getAddress());

            //
            for (Account account : accounts) {
                // список адресов на аккаунте
                List<Address> addresses = addressRepository.findByUserAndAccount(user, account);

                // выбрать нужный
                for (Address a : addresses) {
                    if (a.getBuilding().getBuilding().equalsIgnoreCase(addressRecord.getBuilding())) {
                        address = a;
                        break;
                    }
                }

                if (address != null) {
                    foundAccount = account;
                    break;
                }
            }
        }

        if (foundAccount == null)
            throw new IllegalStateException("Адрес " + data.getAddress() + " не найден");

        Payment p = new Payment();
        p.setUser(service.getUser());
        p.setAccount(foundAccount);
        p.setAddress(address);
        p.setPayment(data.getAmount());
        p.setService(service);
        p.setLastUpload(user.getLastUpload());
        p.setTimestamp(DateUtils.parseExternalPaymentTimestamp(data.getTimestamp()));

        p = paymentRepository.save(p);
        log.debug(">> Добавили платеж{}", data);

        schedulePaymentProcessing(p);
    }

    private void schedulePaymentProcessing(Payment p) throws SchedulerException {
        JobDetail detail = createJobDetails(p);
        Trigger trigger = createJobTrigger(detail, DateUtils.now());
        scheduler.scheduleJob(detail, trigger);
    }

    private Trigger createJobTrigger(JobDetail detail, ZonedDateTime time) {
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity(detail.getKey().getName(), "payment-append-triggers")
                .withDescription("New Payment add trigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    private JobDetail createJobDetails(Payment p) {
        if (p.getId() == null)
            throw new FileNotFoundException("Payment doesn't have row ID");

        JobDataMap data = new JobDataMap();
        data.put("id", p.getId());

        return JobBuilder.newJob(PaymentAppendJob.class)
                .withIdentity("Payment_" + p.getPayment() + "_" + System.currentTimeMillis(), "payment-append-triggers")
                .withDescription("Process payment " + p.getId())
                .usingJobData(data)
                .storeDurably()
                .build();
    }

    public Page<PaymentDto> findByUser(User user, Pageable pageable) {
        ZonedDateTime lastTwoMonths = DateUtils.now()
                .minusMonths(2)
                .truncatedTo(ChronoUnit.DAYS);

        return paymentRepository
                .findByUserAndTimestampGreaterThanEqualOrderByTimestampDesc(user, lastTwoMonths, pageable)
                .map(PaymentDto::of);
    }

    public Page<PaymentDto> findByCriteria(User user, PaymentSearchCriteria criteria) {
        return paymentRepository
                .findAll(PaymentSpecs.accordingToReportProperties(user, criteria), criteria.getPageable())
                .map(PaymentDto::of);
    }

    public List<Payment> findPaymentsByCriteria(User user, PaymentSearchCriteria criteria) {
        return paymentRepository
                .findAll(PaymentSpecs.accordingToReportProperties(user, criteria), criteria.getPageable())
                .getContent();
    }
}
