package controllers

import scala.io.Source

object HelpersForSpec {
  object FileHelper {
    def loadTestFileContent(filename: String): String =
      Source
        .fromFile(s"test/data/$filename")(scala.io.Codec.UTF8.name)
        .getLines()
        .mkString
  }
}
