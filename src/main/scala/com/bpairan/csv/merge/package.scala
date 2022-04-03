package com.bpairan.csv

import java.nio.file.{Files, Path}

/**
 * Created by Bharathi Pairan on 02/04/2022.
 */
package object merge {

  case class AggPaths(size: Long = 0, paths: Seq[Path] = Seq.empty) {
    def add(newSize: Long, newPath: Path): AggPaths = {
      this.copy(size = newSize, paths = this.paths :+ newPath)
    }
  }

  implicit class GroupedOnSize(val inputPaths: Seq[Path]) extends AnyVal {
    def groupedOnSize(thresholdBytes: Long): Seq[Seq[Path]] = {
      inputPaths.foldLeft(Seq[AggPaths]()) { case (aggPaths, path) =>
        val currentSize = Files.size(path)
        val addedSize = aggPaths.lastOption.map(_.size).getOrElse(0L) + currentSize
        if (addedSize <= thresholdBytes && aggPaths.nonEmpty) {
          aggPaths.updated(aggPaths.size - 1, aggPaths.last.add(addedSize, path))
        } else {
          aggPaths :+ AggPaths(currentSize, Seq(path))
        }
      }.map(_.paths)
    }
  }

}
