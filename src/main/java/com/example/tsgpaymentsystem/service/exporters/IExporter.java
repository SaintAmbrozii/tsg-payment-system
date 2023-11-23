package com.example.tsgpaymentsystem.service.exporters;

import com.example.tsgpaymentsystem.domain.Payment;

import java.util.List;

public interface IExporter {

    byte[] export(List<Payment> payments, boolean withDates) throws Exception;

    //список платежей по датам экспортруется в csv и xml
}
