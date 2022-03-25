package com.bpairan.csv.merge

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import scala.io.Source
import scala.util.Using

/**
 * Created by Bharathi Pairan on 24/03/2022.
 */
class CsvMergerSpec extends AnyFlatSpec with EitherValues with Matchers {

  trait TestCase {
    val prefix = "csv-merge"
    val tempDir: Path = Files.createTempDirectory(prefix)

    def newFile(fileName: String = UUID.randomUUID().toString, folder: String = ""): Path = {
      tempDir.resolve(folder).resolve(fileName)
    }

    def testResource(path: String): Path = {
      Paths.get("").toAbsolutePath.resolve("src/test/resources").resolve(Paths.get(path)).normalize()
    }

    implicit class InputPaths(paths: Seq[String]) {
      def toInputPaths: Seq[Path] = {
        paths.map(testResource).map(path => Files.copy(path, tempDir.resolve(path.getFileName)))
      }
    }

  }

  it should "be merged for unix line separator" in new TestCase {
    val inputPaths: Seq[Path] = Seq("unix/File1.split", "unix/File2.split", "unix/File3.split").toInputPaths
    val outputPath: Path = newFile("File1.csv")

    CsvMerger().merge(inputPaths, ".split", ".csv").value shouldBe MergeSuccess(inputPaths, outputPath)

    private val expected = Using(Source.fromFile(testResource("unix/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }

  it should "be empty input" in {
    CsvMerger().merge(Seq.empty, ".split", ".csv").value shouldBe EmptyInput
  }

  it should "be merged for classic-mac line separator" in new TestCase {
    val inputPaths: Seq[Path] = Seq("classic-mac/File1.split", "classic-mac/File2.split", "classic-mac/File3.split").toInputPaths
    val outputPath: Path = newFile("File1.csv")

    CsvMerger().merge(inputPaths, ".split", ".csv").value shouldBe MergeSuccess(inputPaths, outputPath)

    private val expected = Using(Source.fromFile(testResource("classic-mac/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }

  it should "be merged for windows line separator" in new TestCase {
    val inputPaths: Seq[Path] = Seq("windows/File1.split", "windows/File2.split", "windows/File3.split").toInputPaths
    val outputPath: Path = newFile("File1.csv")

    CsvMerger().merge(inputPaths, ".split", ".csv").value shouldBe MergeSuccess(inputPaths, outputPath)

    private val expected = Using(Source.fromFile(testResource("windows/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }
}
