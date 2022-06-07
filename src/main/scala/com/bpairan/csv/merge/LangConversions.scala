package com.bpairan.csv.merge

import scala.collection.JavaConverters._

/**
 * Created by Bharathi Pairan on 07/06/2022.
 */
object LangConversions {

  def asScalaSeq[T](list: java.util.List[T]): Seq[T] = list.asScala.toSeq

}
