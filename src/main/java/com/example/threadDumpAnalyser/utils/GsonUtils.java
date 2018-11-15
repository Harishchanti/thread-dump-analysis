package com.example.threadDumpAnalyser.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GsonUtils {

    public static Gson GSON =new GsonBuilder().setPrettyPrinting().create() ;
}
