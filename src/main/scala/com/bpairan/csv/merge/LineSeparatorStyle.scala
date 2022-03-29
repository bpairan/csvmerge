package com.bpairan.csv.merge

import com.bpairan.csv.merge.CsvMerger.hasNoLineSeparator

import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/**
 * Created by Bharathi Pairan on 29/03/2022.
 */
sealed trait LineSeparatorStyle

case object NoLineSeparator extends LineSeparatorStyle

case object HasLineSeparator extends LineSeparatorStyle

object LineSeparatorStyle {
  val buffer: ByteBuffer = ByteBuffer.allocate(4)

  def from(in: FileChannel): LineSeparatorStyle = {
    in.read(buffer, in.size() - 4)
    buffer.flip()
    if (hasNoLineSeparator(buffer)) {
      NoLineSeparator
    } else {
      HasLineSeparator
    }
  }
}