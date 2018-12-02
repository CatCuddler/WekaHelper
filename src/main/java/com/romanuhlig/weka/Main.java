package com.romanuhlig.weka;

import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.time.TimeHelper;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZonedDateTime;

public class Main {


    static String inputFilePath = "./inputFrameData/TestUser__Task1__SID1__1542122933.csv";
    static String outputFilePathBase = "./outputResults/";
    static String outputFolderTag = "";


    public static void main(String[] args) throws Exception {

        // FrameDataReader.readFrameData("./inputFrameData/TestUser__Task1__SID1__1542122933.csv");

        String startTime = TimeHelper.getDateWithSeconds();

        String outputFilePath = outputFilePathBase + startTime + outputFolderTag;

        FrameDataReader.createFeatureSets(inputFilePath, outputFilePath);

    }

}
