package com.example.tsgpaymentsystem.job;

import com.example.tsgpaymentsystem.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
@Slf4j
public class PaymentAppendJob extends QuartzJobBean {

    private final PaymentService paymentService;

    public PaymentAppendJob(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        JobDetail jobDetail = context.getJobDetail();
        log.debug(">>> Execution job {} ", jobDetail.getKey());
        Long id = (Long) jobDetail.getJobDataMap().get("id");

        try {
            paymentService.recalculationOfDebts(id);

        } catch (Throwable any) {
            log.error("Пересчет остатков не может быть выполнен.", any);
            throw new JobExecutionException(any);
        }

    }
}
