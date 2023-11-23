package com.example.tsgpaymentsystem.service.exporters;

public class ExporterFactory {

    public ExporterFactory() {
    }
    public static IExporter getXlsExporter() {
        return new XlsExporter();
    }

    public static IExporter getCsvExporter() {
        return new CsvExporter();
    }
}
