package com.romanuhlig.weka.data;

import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FrameDataReader {

    static final String outputBaseFolder = "features";

    public static FrameDataSet readFrameData(String filePath) {

        try (
                // reader in resource statement is automatically closed on failure
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
        ) {
            // read data directly into annotated fields of FrameData class
            @SuppressWarnings("unchecked")
            CsvToBean<FrameData> csvToBean =
                    new CsvToBeanBuilder(reader)
                            .withType(FrameData.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withSeparator(';')
                            .build();
            List<FrameData> frameDataFromFile = csvToBean.parse();

            FrameDataSet frameDataSet = new FrameDataSet(frameDataFromFile);

/*            for (int i = 0; i < frameDataSet.getAllFrameData().get(0).size(); i++) {
                System.out.println("linear acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getLinAccelerationX());
                System.out.println("angular acceleration " + frameDataSet.getAllFrameData().get(0).get(i).getAngAccelerationX());
 }
 */

            System.out.println(frameDataSet.getSubject());
            System.out.println(frameDataSet.getActivity());

            return frameDataSet;

        } catch (
                Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }

    }


    public static void createFeatureSets(String inputFilePath, String outputFilePath) {

        // create feature table output folder
        String outputFeaturesFilePath = outputFilePath + "/" + outputBaseFolder + "/";
        new File(outputFeaturesFilePath).mkdirs();

        // read existing data
        // get files in path
        File inputFolder = new File(inputFilePath);
        File[] listOfInputFiles = inputFolder.listFiles();

        // collect window data from all files
        ArrayList<FrameDataSet> windows = new ArrayList<>();
        for (File inputFile : listOfInputFiles) {
            // exclude lock files
            if (inputFile.getName().contains(".~")){
                continue;
            }
            System.out.println(inputFile.getPath());
            FrameDataSet dataSet = readFrameData(inputFile.getPath());
            ArrayList<FrameDataSet> dataSetWindows = dataSet.separateFrameDataIntoWindows(5, 0.1);
            windows.addAll(dataSetWindows);
            System.out.println("windows: " + windows.size());
        }

        //FrameDataSet dataSet = readFrameData(inputFilePath);

        // separate data into windows
        //   ArrayList<FrameDataSet> windows = dataSet.separateFrameDataIntoWindows(5, 0.1);
        //  System.out.println("windows: " + windows.size());

        // prepare header
        List<String> sensorTypes = windows.get(0).getSensorPositions();
        ArrayList<String> headerFields = new ArrayList<>();
        for (String sensorType : sensorTypes) {
            headerFields.add(sensorType + "_maximumHeight");
            headerFields.add(sensorType + "_averageHeight");
        }
        headerFields.add("activity");

        ArrayList<ArrayList<String>> dataFields = new ArrayList<>();

        // extract features
        // for each window
        for (FrameDataSet singleWindow : windows) {

            // create new data line
            ArrayList<String> dataLine = new ArrayList<>();
            dataFields.add(dataLine);
            ArrayList<ArrayList<FrameData>> frameDataSet = singleWindow.getAllFrameData();

            for (ArrayList<FrameData> singleSensor : frameDataSet) {

                // calculate features for sensor
                double maximumHeight = 0;
                double averageHeight = 0;

                for (FrameData frameData : singleSensor) {

                    // if (frameData.getSensorPosition().equals("head"))
                    //     System.out.println(frameData.getCalPosZ()); // correctly shows different values from original file

                    if (frameData.getCalPosZ() > maximumHeight) {
                        maximumHeight = frameData.getCalPosZ();
                    }
                    averageHeight += frameData.getCalPosZ();
                }
                averageHeight /= singleSensor.size();

                // add features to data line
                dataLine.add(Double.toString(maximumHeight));
                dataLine.add(Double.toString(averageHeight));

                //  System.out.println("max          " + maximumHeight);
                //  System.out.println("avg          " + averageHeight);

            }

            dataLine.add(singleWindow.getActivity());

        }

        // write feature data in file
        try (
                Writer writer = Files.newBufferedWriter(Paths.get(outputFeaturesFilePath + "testFile.csv"));

                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            //  String[] headerRecord = {"Name", "Email", "Phone", "Country"};
            csvWriter.writeNext(headerFields.toArray(new String[headerFields.size()]));

            for (ArrayList<String> line : dataFields) {
                csvWriter.writeNext(line.toArray(new String[line.size()]));
            }

        } catch (Exception e) {
            System.err.println("unable to write file " + outputFeaturesFilePath);
        }


    }


}


































