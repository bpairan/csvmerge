package com.bpairan.csv.merge

import java.nio.file.{Files, Path, Paths}
import java.util.UUID

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