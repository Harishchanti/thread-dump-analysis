package com.example.threadDumpAnalyser.service;

import com.example.threadDumpAnalyser.controller.config.ThreadAnalysisConfig;
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

    final static String FAST_THREAD_API = "http://api.fastthread.io/fastthread-api?apiKey=44f1da77-a8b8-4f2e-bd00-0a9f1c0d9b1e";


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
                    .post(FAST_THREAD_API)
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


    @Scheduled(cron = "${threadAnalysis.cronPattern}")
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
