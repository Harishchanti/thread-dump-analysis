package com.example.threadDumpAnalyser.service;

import com.example.threadDumpAnalyser.config.ThreadAnalysisConfig;
import com.example.threadDumpAnalyser.model.ThreadAnalysisReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

@Service
@Slf4j
public class ThreadDumpAnalyzerService {

    @Autowired
    JavaMailSender javaMailSender;

    @Autowired
    ThreadAnalysisConfig threadAnalysisConfig;


    private void sendMail(String[] recipients, String message) {

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(recipients);
        mailMessage.setFrom("FastThread");
        mailMessage.setSubject("Thread Dump Analysis: " + threadAnalysisConfig.getServiceName());
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);
    }


    private ThreadAnalysisReport generateReport() throws UnirestException, IOException {
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);

        FileWriter fw = null;
        try {
            fw = new FileWriter("threadDump.txt");
            for (ThreadInfo threadInfo : threadInfos) {
                fw.write('"');
                fw.write(threadInfo.getThreadName());
                fw.write("\" ");
                final Thread.State state = threadInfo.getThreadState();
                fw.write("\n   java.lang.Thread.State: ");
                fw.write(state.toString());
                final StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
                for (final StackTraceElement stackTraceElement : stackTraceElements) {
                    fw.write("\n        at ");
                    fw.write(stackTraceElement.toString());
                }
                fw.write("\n\n");
            }


            HttpResponse<ThreadAnalysisReport> response = Unirest
                    .post(threadAnalysisConfig.getUrl()).queryString("apiKey",threadAnalysisConfig.getApikey())
                    .field("file", new File("threadDump.txt"))
                    .asObject(ThreadAnalysisReport.class);

            ThreadAnalysisReport report = response.getBody();

            return report;
        } catch (UnirestException | IOException e) {
            throw e;
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    public ThreadAnalysisReport invoke() throws UnirestException, IOException {

        ThreadAnalysisReport report = generateReport();
        sendMail(threadAnalysisConfig.getRecipients(), report.toString());
        return report;

    }


    @Scheduled(cron = "${threadAnalysis.cronpattern}")
    private void analyse() {

        try {
            log.info("Runnin thread analysis cron");
            ThreadAnalysisReport report = generateReport();

            if (report.getProblems() != null && report.getProblems().size() > 0) {
                sendMail(threadAnalysisConfig.getRecipients(), report.toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @PostConstruct
    public void setup() {
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {
            private ObjectMapper jacksonObjectMapper = new ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

}
