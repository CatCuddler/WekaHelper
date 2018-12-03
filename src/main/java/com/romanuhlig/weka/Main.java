package com.romanuhlig.weka;

import com.romanuhlig.weka.data.FrameDataReader;
import com.romanuhlig.weka.time.TimeHelper;

public class Main {


    static String inputFilePath = "./inputFrameData";
    static String outputFilePathBase = "./outputResults/";
    static String outputFolderTag = "";


    public static void main(String[] args) throws Exception {

        // FrameDataReader.readFrameData("./inputFrameData/TestUser__Task1__SID1__1542122933.csv");

        String startTime = TimeHelper.getDateWithSeconds();

        String outputFilePath = outputFilePathBase + startTime + outputFolderTag;

        FrameDataReader.createFeatureSets(inputFilePath, outputFilePath);

    }

}
