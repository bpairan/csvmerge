package com.bpairan.csv.merge

import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit

/**
 * Created by Bharathi Pairan on 25/03/2022.
 */
object PerformanceTest {
  val log: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    val inputs: Seq[Path] = (1 to 50).map(i => testResource(s"medium/us-futures-tick$i.split"))
    val outputPath = testResource("medium/us-futures-tick.csv")
    val start = System.nanoTime()
    CsvMerger(128).merge(inputs, outputPath)
    val end = System.nanoTime()
    log.info(s"Total time: ${TimeUnit.NANOSECONDS.toMillis(end - start)} ms")
  }

  def testResource(path: String): Path = {
    Paths.get("").toAbsolutePath.resolve("src/test/resources").resolve(Paths.get(path)).normalize()
  }
}
