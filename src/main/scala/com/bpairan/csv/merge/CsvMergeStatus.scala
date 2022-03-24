package com.bpairan.csv.merge

import java.nio.file.Path

/**
 * Created by Bharathi Pairan on 19/03/2022.
 */
sealed trait CsvMergeStatus

final case class MergeSuccess(inputPaths: Seq[Path], outputPath: Path) extends CsvMergeStatus

final case class NoFilesToMerge(inputRegex: Option[String]) extends CsvMergeStatus

case object EmptyInput extends CsvMergeStatus

final case class ErrorInWritingOutput(errorMessage: String) extends CsvMergeStatus
