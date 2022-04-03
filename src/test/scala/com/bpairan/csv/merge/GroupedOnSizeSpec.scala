package com.bpairan.csv.merge

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.nio.file.Path

/**
 * Created by Bharathi Pairan on 02/04/2022.
 */
class GroupedOnSizeSpec extends AnyFlatSpec with Matchers {
  it should "group paths separately" in new TestCase {
    val inputPaths: Seq[Path] = Seq("unix/File1.split", "unix/File2.split", "unix/File3.split").toInputPaths
    val thresholdBytes = 20
    inputPaths.groupedOnSize(thresholdBytes) shouldBe Seq(Seq(inputPaths.head), Seq(inputPaths(1)), Seq(inputPaths(2)))
  }

  it should "combine couple of path together" in new TestCase {
    val inputPaths: Seq[Path] = Seq("unix/File1.split", "unix/File2.split", "unix/File3.split").toInputPaths
    val thresholdBytes = 40
    inputPaths.groupedOnSize(thresholdBytes) shouldBe Seq(Seq(inputPaths.head, inputPaths(1)), Seq(inputPaths(2)))
  }

  it should "combine all paths together" in new TestCase {
    val inputPaths: Seq[Path] = Seq("unix/File1.split", "unix/File2.split", "unix/File3.split").toInputPaths
    val thresholdBytes = 60
    inputPaths.groupedOnSize(thresholdBytes) shouldBe Seq(inputPaths)
  }
}
