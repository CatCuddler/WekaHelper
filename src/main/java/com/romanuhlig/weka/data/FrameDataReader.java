package com.romanuhlig.weka.data;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FrameDataReader {

    public static FrameDataSet readFrameData(String filePath) throws IOException {

        try (
                // reader in resource statement is automatically closed on failure
                Reader reader = Files.newBufferedReader(Paths.get(filePath));
        ) {
            // read data directly into annotated fields of FrameData class
            CsvToBean<FrameData> csvToBean =
                    new CsvToBeanBuilder(reader)
                            .withType(FrameData.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .withSeparator(';')
                            .build();
            List<FrameData> frameDataFromFile = csvToBean.parse();

            FrameDataSet frameDataSet = new FrameDataSet(frameDataFromFile);

            for (int i = 0; i < frameDataSet.allFrameData.get(0).size(); i++) {
                System.out.println(frameDataSet.allFrameData.get(0).get(i).getAccelerationX());
            }

            return frameDataSet;

        } catch (Exception e) {
            System.err.println("unable to read file " + filePath);
            return null;
        }
    }
}
