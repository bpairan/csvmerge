package com.bpairan.csv.merge

/**
 * Created by Bharathi Pairan on 29/03/2022.
 */
sealed trait LineSeparator {
  def toBytes: Array[Byte]
}

case object LF extends LineSeparator {
  override def toBytes: Array[Byte] = Array('\n'.toByte)
}

case object CR extends LineSeparator {
  override def toBytes: Array[Byte] = Array('\r'.toByte)
}

case object CRLF extends LineSeparator {
  override def toBytes: Array[Byte] = Array('\r'.toByte, '\n'.toByte)
}
