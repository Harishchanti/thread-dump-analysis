package com.example.threadDumpAnalyser.model;

import com.example.threadDumpAnalyser.utils.GsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThreadAnalysisReport implements Serializable {


    @JsonProperty("problem")
    List<ProblemDescription> problems = Lists.newArrayList();

    @JsonProperty("threadDumpReport")
    List<ThreadDumpReport> threadDumpReports = Lists.newArrayList();

    @JsonProperty("responseId")
    String responseId;

    @JsonProperty("graphURL")
    URI fullReport;



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ProblemDescription
    {
        String level;
        String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ThreadState
    {
        String state;
        int threadCount;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ThreadDumpReport
    {
        @JsonProperty("threadState")
        List<ThreadState> threadStates = Lists.newArrayList();

    }

    @Override
    public String toString() {
        return GsonUtils.GSON.toJson(this);
    }
}