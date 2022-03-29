package com.bpairan.csv.merge

/**
 * Created by Bharathi Pairan on 28/03/2022.
 */
case class NewLine(idx: Int, isFound: Boolean, separator: Option[LineSeparator] = None) {
  def copyFrom(that: NewLine): NewLine = {
    this.copy(idx = this.idx + that.idx, isFound = that.isFound, separator = that.separator)
  }

  def addIdx(that: Int): NewLine = {
    this.copy(idx = this.idx + that)
  }
}
