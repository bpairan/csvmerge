# csvmerge

Lightning fast Zero Copy CSV files merger for JVM

Merges input CSV files without loading them into memory

Supports file merging with Unix(`\n`) new line and Classic Mac OS(`\r`) carriage return. 

Windows line separator `\r\n` is currently not supported but expected soon.

## usage

Just pass in Seq of input file `java.io.Path`, file extension of input paths and the expected output file extension

```
import com.bpairan.csv.merge.CsvMerger

CsvMerger().merge(Seq(inputFilePaths), ".split", ".csv")
```

By default, CsvMerger allocates buffer size of 256 in the heap, but size of the buffer can be input while constructing `CsvMerger`

```
import com.bpairan.csv.merge.CsvMerger

CsvMerger(128).merge(Seq(inputFilePaths), ".split", ".csv")
```



