package com.bpairan.csv.merge

import com.bpairan.csv.merge.CsvMerger.CsvMergeSuccessOr

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
class CsvMerger(bufferSize: Int, lineSeparator: Byte) {
  private val buffer = ByteBuffer.allocate(bufferSize)

  /**
   * Find first position of New Line(\n) in the FileChannel
   *
   * @param in             input FileChannel
   * @param buffer         byte buffer
   * @param newLineIdx     index of new line (\n) or EOF
   * @param isNewLineFound true on first occurrence of new line(\n) or EOF
   * @return Index of first occurrence of new line(\n) or EOF
   */
  @tailrec
  final def firstPositionAfterHeader(in: FileChannel, buffer: ByteBuffer, newLineIdx: Int = 0, isNewLineFound: Boolean = false): Int = {
    if (isNewLineFound || newLineIdx == in.size()) {
      buffer.clear()
      newLineIdx
    } else {
      in.read(buffer)
      buffer.flip()
      val (isNewLineFoundNow, bufferIdx) = findNewLinePosition(buffer)
      buffer.clear()
      firstPositionAfterHeader(in, buffer, newLineIdx + bufferIdx, isNewLineFoundNow)
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
   * If input file end with empty new line then they are copied as is, you must clean the files before merging
   *
   * @param in        input file FileChannel
   * @param out       output FileChannel
   * @param inIdx     index of input file path
   * @param inputSize total number of file paths to be merged
   */
  private final def copyFile(in: FileChannel, out: FileChannel, inIdx: Int, inputSize: Int): Unit = {
    //Keep the header from first file for subsequent files find the position after '\n'
    val pos = if (inIdx == 0) 0 else firstPositionAfterHeader(in, buffer)

    // if last file then retain the new line
    //val count = if (size - 1 == idx) in.size() - pos else in.size() - pos - 1
    val count = in.size() - pos

    in.transferTo(pos, count, out)
    in.close()
  }

  /**
   * Iterates the buffer until New Line (\n) is encountered
   *
   * @param buffer input byte buffer
   * @param idx    index on line
   * @return true if New Line found, false if buffer limit is reached and the index on line
   */
  @tailrec
  private final def findNewLinePosition(buffer: ByteBuffer, idx: Int = 0): (Boolean, Int) = {
    if (idx == buffer.limit()) {
      false -> idx
    } else {
      val c = buffer.get(idx)
      if (c == lineSeparator) {
        true -> idx
      } else {
        findNewLinePosition(buffer, idx + 1)
      }
    }
  }
}

object CsvMerger {
  type CsvMergeSuccessOr[T] = Either[T, CsvMergeStatus]

  def apply(bufferSize: Int = 256, lineSeparator: Byte = '\n'.toByte): CsvMerger = new CsvMerger(bufferSize, lineSeparator)
}
