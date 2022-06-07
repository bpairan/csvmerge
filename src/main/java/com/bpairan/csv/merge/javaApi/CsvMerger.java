package com.bpairan.csv.merge.javaApi;

import com.bpairan.csv.merge.CsvMergeStatus;
import com.bpairan.csv.merge.LangConversions;

import java.nio.file.Path;
import java.util.List;

/**
 * Java API wrapper for CsvMerger
 * <p>
 * Created by Bharathi Pairan on 07/06/2022.
 */
public class CsvMerger {
    private int bufferSize = 256;
    private boolean hasHeader = true;


    public CsvMerger(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public CsvMerger(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public CsvMerger(int bufferSize, boolean hasHeader) {
        this.bufferSize = bufferSize;
        this.hasHeader = hasHeader;
    }

    /**
     * @param inputPaths List of input file paths
     * @param outputPath output file path
     * @return CsvMergeStatus
     * @throws CsvMergerException if merge fails
     */
    public CsvMergeStatus merge(List<Path> inputPaths, Path outputPath) throws CsvMergerException {
        return new com.bpairan.csv.merge.CsvMerger(bufferSize, hasHeader).mergeNative(LangConversions.asScalaSeq(inputPaths), outputPath);
    }

}
