package com.bpairan.csv.merge

import cats.implicits._
import com.bpairan.csv.merge.CsvMerger.{CRByte, LFByte}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.ByteBuffer
import java.nio.file.{Files, Path, Paths}
import java.util.UUID
import scala.io.Source
import scala.util.Using

/**
 * Created by Bharathi Pairan on 24/03/2022.
 */
class CsvMergerSpec extends AnyFlatSpec with Matchers {

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

    CsvMerger().merge(inputPaths, outputPath) shouldBe MergeSuccess(inputPaths, outputPath).validNel

    private val expected = Using(Source.fromFile(testResource("unix/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }

  it should "be empty input" in {
    CsvMerger().merge(Seq.empty, Paths.get("")) shouldBe EmptyInput.validNel
  }

  it should "be merged for classic-mac line separator" in new TestCase {
    val inputPaths: Seq[Path] = Seq("classic-mac/File1.split", "classic-mac/File2.split", "classic-mac/File3.split").toInputPaths
    val outputPath: Path = newFile("File1.csv")

    CsvMerger().merge(inputPaths, outputPath) shouldBe MergeSuccess(inputPaths, outputPath).validNel

    private val expected = Using(Source.fromFile(testResource("classic-mac/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }

  it should "be merged for windows line separator" in new TestCase {
    val inputPaths: Seq[Path] = Seq("windows/File1.split", "windows/File2.split", "windows/File3.split").toInputPaths
    val outputPath: Path = newFile("File1.csv")

    CsvMerger().merge(inputPaths, outputPath) shouldBe MergeSuccess(inputPaths, outputPath).validNel

    private val expected = Using(Source.fromFile(testResource("windows/ExpectedFile1.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)
    actual shouldBe expected
  }

  it should "merge files without headers" in new TestCase {
    val inputPaths: Seq[Path] = Seq("no-headers/File1.split", "no-headers/File2.split", "no-headers/File3.split").toInputPaths
    val outputPath: Path = newFile("ExpectedFile.csv")

    CsvMerger(hasHeader = false).merge(inputPaths, outputPath) shouldBe MergeSuccess(inputPaths, outputPath).validNel

    private val expected = Using(Source.fromFile(testResource("no-headers/ExpectedFile.csv").toFile))(_.mkString)
    private val actual = Using(Source.fromFile(outputPath.toFile))(_.mkString)

    actual shouldBe expected
  }

  it should "be LF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isLF(buffer) shouldBe true
  }

  it should "be LF when repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.clear()
    buffer.put(LFByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isLF(buffer) shouldBe true
  }

  it should "not be LF when CR" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isLF(buffer) shouldBe false
  }

  it should "not be LF when CRLF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isLF(buffer) shouldBe false
  }

  it should "be CR" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isCR(buffer) shouldBe true
  }

  it should "be CR when repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isCR(buffer) shouldBe true
  }

  it should "not be CR when LF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCR(buffer) shouldBe false
  }

  it should "not be CR when CRLF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCR(buffer) shouldBe false
  }

  it should "be CRLF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRLF(buffer) shouldBe true
  }

  it should "be CRLF when repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRLF(buffer) shouldBe true
  }

  it should "not be CRLF when LF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRLF(buffer) shouldBe false
  }

  it should "not be CRLF when CR" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isCRLF(buffer) shouldBe false
  }

  "LF" should "be repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isLFRepeated(buffer) shouldBe true
  }

  it should "not be repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isLFRepeated(buffer) shouldBe false

    buffer.clear()
    buffer.put(CRByte)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isLFRepeated(buffer) shouldBe false
  }

  "CR" should "be repeated" in {
    val buffer = ByteBuffer.allocate(2)
    buffer.put(CRByte)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isCRRepeated(buffer) shouldBe true
  }

  it should "not be repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRRepeated(buffer) shouldBe false

    buffer.clear()
    buffer.put(LFByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRRepeated(buffer) shouldBe false
  }

  "CRLF" should "be repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRLFRepeated(buffer) shouldBe true
  }

  it should "not be repeated" in {
    val buffer = ByteBuffer.allocate(2)
    buffer.put(CRByte)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.isCRLFRepeated(buffer) shouldBe false

    buffer.clear()
    buffer.put(LFByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.isCRLFRepeated(buffer) shouldBe false
  }

  "hasLineSeparator" should "be true for CR" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be true for LF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be true for CRLF" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be true when CR is repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(CRByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be true when LF is repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(LFByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be true when CRLF is repeated" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.put(CRByte)
    buffer.put(LFByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe true
  }

  it should "be false" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(','.toByte)
    buffer.put('B'.toByte)
    buffer.put(','.toByte)
    buffer.put('C'.toByte)
    buffer.flip()
    CsvMerger.hasLineSeparator(buffer) shouldBe false
  }

  "hasNoLineSeparator" should "be true" in {
    val buffer = ByteBuffer.allocate(4)
    buffer.put(','.toByte)
    buffer.put('B'.toByte)
    buffer.put(','.toByte)
    buffer.put('C'.toByte)
    buffer.flip()
    CsvMerger.hasNoLineSeparator(buffer) shouldBe true
  }
}
