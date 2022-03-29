package com.bpairan.csv.merge

import cats.data.ValidatedNel
import cats.implicits._
import com.bpairan.csv.merge.CsvMerger.{CRByte, CsvMergeErrorsOr, LFByte, PathOps}
import org.slf4j.LoggerFactory

import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption.{CREATE, READ, TRUNCATE_EXISTING, WRITE}
import scala.annotation.tailrec
import scala.util.Using

/**
 * Merges all csv files into one single file
 * Csv files with or without headers can be merged
 * Created by Bharathi Pairan on 19/03/2022.
 */
class CsvMerger(bufferSize: Int, hasHeader: Boolean) {
  private val log = LoggerFactory.getLogger(getClass)
  private val buffer = ByteBuffer.allocate(bufferSize)
  private val writeBuffer = ByteBuffer.allocate(2)

  /**
   * Merges input file paths to specified file extension. Keeps header from first file and drops it for rest of them
   *
   * @param inputPaths input file paths
   * @param outputPath output file path
   * @return [[CsvMergeStatus]] if successful or all [[CsvMergeError]]
   */
  def merge(inputPaths: Seq[Path], outputPath: Path): CsvMergeErrorsOr[CsvMergeStatus] = {
    if (inputPaths.isEmpty) {
      EmptyInput.validNel
    } else {
      doMerge(inputPaths, outputPath)
    }
  }

  /**
   * Iterates the list of input paths and merges them sequentially.
   *
   * @param inputPaths list of input paths
   * @param outputPath output file path
   * @return [[CsvMergeStatus]] or [[CsvMergeError]]
   */
  private def doMerge(inputPaths: Seq[Path], outputPath: Path): CsvMergeErrorsOr[CsvMergeStatus] = {
    Using(FileChannel.open(outputPath, CREATE, WRITE, TRUNCATE_EXISTING)) { out =>
      val size = inputPaths.size
      inputPaths.toList.zipWithIndex.map { case (inPath, idx) =>
        Using(inPath.toReadFileChannel) { in =>
          copyFile(in, out, idx, size)
        }.fold(t => CsvMergeError(s"Cannot open input file ($inPath): ${t.getMessage}").invalidNel, _ => ().validNel)
      }.sequence_
        .map(_ => MergeSuccess(inputPaths, outputPath))
    }.fold(t => CsvMergeError(s"Cannot open file ($outputPath) to write: ${t.getMessage}").invalidNel, identity)
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
    val newLine = if (hasHeader && inIdx > 0) skipHeader(in) else NewLine(0, isFound = false)

    // if last file then retain the line separator
    //val bytesSize = if (inputSize - 1 == idx) in.size() - startPosition else in.size() - startPosition - 1
    val bytesSize = in.size() - newLine.idx

    LineSeparatorStyle.from(in) match {
      case NoLineSeparator =>
        log.info("Has no line separator")
        in.transferTo(newLine.idx, bytesSize, out)
        if (inputSize - 1 != inIdx) {
          addLineSeparator(in, out)
        }
      case HasLineSeparator =>
        log.info("Has line separator")
        if (inputSize - 1 != inIdx) {
          in.transferTo(newLine.idx, bytesSize, out)
        } else {
          in.transferTo(newLine.idx, bytesSize - 1, out)
        }
    }

    log.debug(s"output file size:${out.size()}")
  }

  /**
   * Finds the line separator of the input file and adds to the out channel
   *
   * @param in  input [[FileChannel]]
   * @param out output [[FileChannel]]
   */
  private def addLineSeparator(in: FileChannel, out: FileChannel): Unit = {
    writeBuffer.clear()
    in.position(0)
    val newLine = skipHeader(in)
    newLine.separator match {
      case Some(value) => value.toBytes.foreach(writeBuffer.put)
      case None => writeBuffer.put(LFByte)
    }
    writeBuffer.flip()
    out.write(writeBuffer)
  }

  /**
   * Find first position of line separator in the FileChannel
   *
   * @param in      input FileChannel
   * @param newLine [[NewLine]]
   * @return Index of first occurrence of line separator or EOF
   */
  @tailrec
  final def skipHeader(in: FileChannel, newLine: NewLine = NewLine(0, isFound = false)): NewLine = {
    if (newLine.isFound || newLine.idx == in.size()) {
      buffer.clear()
      //skip the line separator
      newLine.addIdx(1)
    } else {
      in.read(buffer)
      buffer.flip()
      val _lineSeparator = findNewLine()
      buffer.clear()
      skipHeader(in, newLine.copyFrom(_lineSeparator))
    }
  }

  /**
   * Iterates the buffer until line separator is encountered
   *
   * @param idx index on line
   * @return tuple of values representing if index was found and the index
   *         true if line separator is found, false if buffer limit is reached and the index on line
   */
  @tailrec
  private final def findNewLine(idx: Int = 0, hasCR: Boolean = false): NewLine = {
    if (idx == buffer.limit()) {
      NewLine(idx, isFound = false)
    } else {
      val c = buffer.get(idx)
      if (hasCR) {
        if (c == LFByte) {
          NewLine(idx, isFound = true, CRLF.some) // CRLF
        } else {
          NewLine(idx - 1, isFound = true, CR.some) // CR
        }
      } else if (c == LFByte) {
        NewLine(idx, isFound = true, LF.some)
      } else if (c == CRByte) { // this could be CR or CRLF
        findNewLine(idx + 1, hasCR = true)
      }
      else {
        findNewLine(idx + 1)
      }
    }
  }
}

object CsvMerger {
  type CsvMergeErrorsOr[T] = ValidatedNel[CsvMergeError, T]

  val LFByte: Byte = '\n'.toByte
  val CRByte: Byte = '\r'.toByte

  def apply(bufferSize: Int = 256, hasHeader: Boolean = true): CsvMerger = new CsvMerger(bufferSize, hasHeader)

  implicit class PathOps(val path: Path) extends AnyVal {
    def toReadFileChannel: FileChannel = {
      FileChannel.open(path, READ)
    }
  }

  def isLF(buffer: ByteBuffer): Boolean = {
    if (buffer.limit() > 1) {
      buffer.get(buffer.limit() - 1) == LFByte && buffer.get(buffer.limit() - 2) != CRByte
    } else {
      buffer.get(buffer.limit() - 1) == LFByte
    }
  }

  def isCR(buffer: ByteBuffer): Boolean = {
    buffer.get(buffer.limit() - 1) == CRByte
  }

  def isCRLF(buffer: ByteBuffer): Boolean = {
    if (buffer.limit() > 1) {
      buffer.get(buffer.limit() - 1) == LFByte && buffer.get(buffer.limit() - 2) == CRByte
    } else {
      false
    }
  }

  def isLFRepeated(buffer: ByteBuffer): Boolean = {
    buffer.get(0) == LFByte && buffer.get(1) == LFByte
  }

  def isCRRepeated(buffer: ByteBuffer): Boolean = {
    buffer.get(0) == CRByte && buffer.get(1) == CRByte
  }

  def isCRLFRepeated(buffer: ByteBuffer): Boolean = {
    buffer.get(0) == CRByte && buffer.get(1) == LFByte && buffer.get(0) == CRByte && buffer.get(1) == LFByte
  }

  def hasLineSeparator(buffer: ByteBuffer): Boolean = {
    isCR(buffer) || isLF(buffer) || isCRLF(buffer)
  }

  def hasNoLineSeparator(buffer: ByteBuffer): Boolean = {
    !hasLineSeparator(buffer)
  }
}
