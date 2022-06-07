# csvmerge

A Fast Zero Copy CSV files merger for JVM that uses very minimal memory footprint

CsvMerger merges input CSV files without loading them into memory, using direct file copy

Original line separators CR, LF or CRLF are retained after merge, any additional end of file line separators are removed

## usage - Scala

Just pass in Seq of input file `java.io.Path` and output file `java.io.Path`

```scala
import com.bpairan.csv.merge.CsvMerger
import java.nio.file.Paths

val inputFilePaths = Seq(Paths.get("File1.csv"), Paths.get("File2.csv"))
val outputPath = Paths.get("output.csv")

CsvMerger().merge(inputFilePaths, outputPath)
```

By default, CsvMerger allocates buffer size of 256 in the heap, but size of the buffer can be input while constructing `CsvMerger`

```scala
import com.bpairan.csv.merge.CsvMerger
import java.nio.file.Paths

val inputFilePaths = Seq(Paths.get("File1.csv"), Paths.get("File2.csv"))
val outputPath = Paths.get("output.csv")

CsvMerger(128).merge(inputFilePaths, outputPath)
```

To merge files without header just set the flag `hasHeader = false`

```scala
import com.bpairan.csv.merge.CsvMerger
import java.nio.file.Paths

val inputFilePaths = Seq(Paths.get("File1.csv"), Paths.get("File2.csv"))
val outputPath = Paths.get("output.csv")

CsvMerger(hasHeader = false).merge(inputFilePaths, outputPath)
```

## usage - Java

Just pass in List of input file `java.io.Path` and output file `java.io.Path`

```java
import com.bpairan.csv.merge.javaApi.CsvMerger;

import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        List<Path> inputPaths = new ArrayList<>();
        inputPaths.add(Paths.get("File1.csv"));
        inputPaths.add(Paths.get("File2.csv"));
        
        Path outputPath = Paths.get("output.csv");
        
        new CsvMerger().merge(inputPaths, outputPath);
        
        // with bufferSize - size allocated to read the input files default is 256
        new CsvMerger(512).merge(inputPaths, outputPath);
        
        //if input file has no header then set false, default is true
        new CsvMerger(false).merge(inputPaths, outputPath);
        
        // to set both buffer and header
        new CsvMerger(512, false).merge(inputPaths, outputPath);
    }
}
```
