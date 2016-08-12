package controllers

import java.io.File
import java.io.PrintWriter

import scala.io.Source
/**
  * Created by ved on 11/8/16.
  */
object Write {
  def main(args: Array[String]) {
    val writer = new PrintWriter(new File("Write.txt"))

    writer.write("Hello Developer, Welcome to Scala Programming.")
    writer.close()

    Source.fromFile("Write.txt").foreach { x => print(x) }
  }

}
