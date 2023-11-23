package com.example.tsgpaymentsystem.service;

import com.example.tsgpaymentsystem.domain.ProcessState;
import com.example.tsgpaymentsystem.domain.UploadData;
import com.example.tsgpaymentsystem.domain.User;
import com.example.tsgpaymentsystem.exception.FileEmptyException;
import com.example.tsgpaymentsystem.exception.FileNotFoundException;
import com.example.tsgpaymentsystem.job.CalculationsJob;
import com.example.tsgpaymentsystem.repository.FileRepository;
import com.example.tsgpaymentsystem.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class UploadService {

    private final Scheduler scheduler;
    private final FileRepository fileRepository;
    private final CalculationProcessorService calculationProcessorService;

    public UploadService(Scheduler scheduler, FileRepository fileRepository, CalculationProcessorService calculationProcessorService) {
        this.scheduler = scheduler;
        this.fileRepository = fileRepository;
        this.calculationProcessorService = calculationProcessorService;
    }


    public void store(User user, MultipartFile[] files, Long[] defaultServiceIds) throws Throwable {
        if (files == null || files.length == 0)
            throw new FileEmptyException("Files cannot be empty");

        UploadData uploadData = createCombinedFile(user, files);
        uploadData = fileRepository.save(uploadData);


        List<UploadData> uploadDataList = new ArrayList<>();
        for (MultipartFile file : files)
            uploadDataList.add(createUploadData(user, file));

        //  log.debug(">>> LOGGED USER > {}", user);
        calculationProcessorService.processCalculations(user, uploadData, uploadDataList, defaultServiceIds);
        // scheduleFileProcessing(uploadData, defaultServiceId);
    }

    private UploadData createCombinedFile(User user, MultipartFile[] files) throws IOException {
        UploadData uploadData = new UploadData();
        uploadData.setName(buildFileName(files));
        uploadData.setContent(buildFileContent(files));
        uploadData.setState(ProcessState.UPLOADED);
        uploadData.setOwner(user);
        uploadData.setTimestamp(DateUtils.todayStart());
        return uploadData;
    }

    private UploadData createUploadData(User user, MultipartFile files) throws IOException {
        UploadData uploadData = new UploadData();
        uploadData.setName(files.getOriginalFilename());
        uploadData.setContent(files.getBytes());
        uploadData.setState(ProcessState.UPLOADED);
        uploadData.setOwner(user);
        uploadData.setTimestamp(DateUtils.todayStart());
        return uploadData;
    }

    private byte[] buildFileContent(MultipartFile[] files) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (MultipartFile f : files) {
            bos.write(f.getBytes());
            bos.write('\n');
        }

        return bos.toByteArray();
    }

    private String buildFileName(MultipartFile[] files) {
        StringBuilder s = new StringBuilder();
        for (MultipartFile f : files)
            s.append(f.getOriginalFilename()).append('\n');

        return s.toString();
    }

    private void scheduleFileProcessing(UploadData uploadData, Long defaultServiceId) throws SchedulerException {
        JobDetail detail = createJobDetails(uploadData, defaultServiceId);
        Trigger trigger = createJobTrigger(detail, DateUtils.now());
        Date date = scheduler.scheduleJob(detail, trigger);
        log.debug(">> scheduleFileProcessing user's {} file {}_{} at {}",
                uploadData.getOwner().getUsername(), uploadData.getId(), uploadData.getName(), date);
        // TODO: stucks here
    }

    private JobDetail createJobDetails(UploadData uploadData, Long defaultServiceId) {

        if (uploadData.getId() == null)
            throw new FileNotFoundException("UploadData doesn't have row ID");

        if (uploadData.getOwner().getId() == null)
            throw new FileNotFoundException("UploadData doesn't have owner's ID");

        JobDataMap data = new JobDataMap();
        data.put("id", uploadData.getId());
        data.put("serviceId", defaultServiceId);

        return JobBuilder.newJob(CalculationsJob.class)
                .withIdentity(uploadData.getOwner().getId() + "_" + uploadData.getName() + "_" + System.currentTimeMillis(), "calculation-file-triggers")
                .withDescription("Process " + uploadData.getName())
                .usingJobData(data)
                .storeDurably()
                .build();
    }

    private Trigger createJobTrigger(JobDetail detail, ZonedDateTime timestamp) {
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity(detail.getKey().getName(), "calculation-file-triggers")
                .withDescription("Calculation file trigger")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();

    }

}
