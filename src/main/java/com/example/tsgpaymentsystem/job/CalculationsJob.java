package com.example.tsgpaymentsystem.job;

import com.example.tsgpaymentsystem.service.CalculationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
public class CalculationsJob extends QuartzJobBean {

    private final CalculationService calculationService;

    public CalculationsJob(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobDetail jobDetail = context.getJobDetail();
        log.debug(">>> Execution job {} ", jobDetail.getKey());
        Long id = (Long) jobDetail.getJobDataMap().get("id");
        Long serviceId = (Long) jobDetail.getJobDataMap().get("serviceId");

        try {
             calculationService.processCalculationsById(id, serviceId);

        } catch (Throwable any) {
            log.error("Обработка начислений не может быть выполнена.", any);
            throw new JobExecutionException(any);
        }
    }
}

