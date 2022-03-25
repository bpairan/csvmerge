package com.bpairan.csv.merge

import com.bpairan.csv.merge.CsvMerger.{CarriageReturn, CsvMergeSuccessOr, LineFeed}

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption.{CREATE, READ, TRUNCATE_EXISTING, WRITE}
import java.nio.file.{Path, Paths}
import scala.annotation.tailrec
import scala.util.Using

/**
 * Merges all csv files into one single file
 * Assumes all files have same number of columns and retains only column names/headers from the first file in the input sequence/list
 * Output file name is generated based on the first input file with the provided extension
 *
 * Created by Bharathi Pairan on 19/03/2022.
 */
class CsvMerger(bufferSize: Int) {
  private val buffer = ByteBuffer.allocate(bufferSize)

  /**
   * Find first position of line separator in the FileChannel
   *
   * @param in             input FileChannel
   * @param buffer         byte buffer
   * @param newLineIdx     index of line separator or EOF
   * @param isNewLineFound true on first occurrence of line separator or EOF
   * @return Index of first occurrence of line separator or EOF
   */
  @tailrec
  final def positionAfterHeader(in: FileChannel, buffer: ByteBuffer, newLineIdx: Int = 0, isNewLineFound: Boolean = false): Int = {
    if (isNewLineFound || newLineIdx == in.size()) {
      buffer.clear()
      newLineIdx
    } else {
      in.read(buffer)
      buffer.flip()
      val (isNewLineFoundNow, bufferIdx) = findNewLinePosition(buffer)
      buffer.clear()
      positionAfterHeader(in, buffer, newLineIdx + bufferIdx, isNewLineFoundNow)
    }
  }

  /**
   * Merges input file paths to specified file extension. Keeps header from first file and drops it for rest of them
   *
   * @param inputPaths          input file paths
   * @param inputFileExtension  file extension of input file paths
   * @param outputFileExtension output file extension
   * @return Throwable - if encountered error or CsvMergeStatus
   */
  def merge(inputPaths: Seq[Path], inputFileExtension: String, outputFileExtension: String): CsvMergeSuccessOr[Throwable] = {
    inputPaths.headOption match {
      case Some(firstFilePath) =>
        val outputPath = Paths.get(firstFilePath.toString.replace(inputFileExtension, outputFileExtension))
        doMerge(inputPaths, outputPath)
      case None => Right(EmptyInput)
    }
  }

  private def doMerge(inputPaths: Seq[Path], outputPath: Path): CsvMergeSuccessOr[Throwable] = {
    Using(FileChannel.open(outputPath, CREATE, WRITE, TRUNCATE_EXISTING)) { out =>
      try {
        val size = inputPaths.size
        inputPaths.zipWithIndex.map { case (path, idx) => FileChannel.open(path, READ) -> idx }.foreach { case (in, idx) => copyFile(in, out, idx, size) }
        Right(MergeSuccess(inputPaths, outputPath))
      } catch {
        case t: Throwable => Left(t)
      }
    }.fold(t => Left(t), identity)
  }

  /**
   * Copies input to output FileChannel by retaining header from first file, subsequent input file headers are ignored
   * If input file end with empty line separator then they are copied as is, you must clean the files before merging if it's not desired
   *
   * @param in        input file FileChannel
   * @param out       output FileChannel
   * @param inIdx     index of input file path
   * @param inputSize total number of file paths to be merged
   */
  private final def copyFile(in: FileChannel, out: FileChannel, inIdx: Int, inputSize: Int): Unit = {
    //Keep the header from first file for subsequent files find the position after line separator
    val pos = if (inIdx == 0) 0 else positionAfterHeader(in, buffer)

    // if last file then retain the line separator
    //val count = if (size - 1 == idx) in.size() - pos else in.size() - pos - 1
    val count = in.size() - pos

    in.transferTo(pos, count, out)
    in.close()
  }

  /**
   * Iterates the buffer until line separator is encountered
   *
   * @param buffer input byte buffer
   * @param idx    index on line
   * @return tuple of values representing if index was found and the index
   *         true if line separator is found, false if buffer limit is reached and the index on line
   */
  @tailrec
  private final def findNewLinePosition(buffer: ByteBuffer, idx: Int = 0): (Boolean, Int) = {
    if (idx == buffer.limit()) {
      false -> idx
    } else {
      val c = buffer.get(idx)
      if (c == LineFeed || c == CarriageReturn) {
        true -> idx
      }
      else {
        findNewLinePosition(buffer, idx + 1)
      }
    }
  }
}

object CsvMerger {
  type CsvMergeSuccessOr[T] = Either[T, CsvMergeStatus]

  val LineFeed: Byte = '\n'.toByte
  val CarriageReturn: Byte = '\r'.toByte

  def apply(bufferSize: Int = 256): CsvMerger = new CsvMerger(bufferSize)
}
