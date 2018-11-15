package com.example.threadDumpAnalyser.controller;

import com.example.threadDumpAnalyser.model.ThreadAnalysisReport;
import com.example.threadDumpAnalyser.service.ThreadDumpAnalyzerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/admin")
@Slf4j
public class ThreadDumpController {

    @Autowired
    ThreadDumpAnalyzerService threadDumpAnalyzer;

    @RequestMapping(value = "/threadDump", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ThreadAnalysisReport threadDump() throws Exception {

        return threadDumpAnalyzer.invoke();
    }


    public void call() {
        log.info("Finally!");
    }

}
