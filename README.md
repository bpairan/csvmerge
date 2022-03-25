# csvmerge

Lightning fast Zero Copy CSV files merger for JVM

Merges input CSV files without loading them into memory

All line separators CR, LF or CRLF are retained as is after merge

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



