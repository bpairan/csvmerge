# csvmerge

A Fast Zero Copy CSV files merger for JVM that uses very minimal memory footprint

CsvMerger merges input CSV files without loading them into memory, using direct file copy

Original line separators CR, LF or CRLF are retained after merge, any additional end of file line separators are removed

## usage

Just pass in Seq of input file `java.io.Path` and output file path

```
import com.bpairan.csv.merge.CsvMerger
val inputFilePaths = Seq(Paths.get("File1.csv"), Paths.get("File2.csv"))
CsvMerger().merge(inputFilePaths, outputPath)
```

By default, CsvMerger allocates buffer size of 256 in the heap, but size of the buffer can be input while constructing `CsvMerger`

```
import com.bpairan.csv.merge.CsvMerger
val inputFilePaths = Seq(Paths.get("File1.csv"), Paths.get("File2.csv"))
CsvMerger(128).merge(inputFilePaths, outputPath)
```

To merge files without header just set the flag `hasHeader = false`

```
import com.bpairan.csv.merge.CsvMerger

CsvMerger(hasHeader = false).merge(inputFilePaths, outputPath)
```


