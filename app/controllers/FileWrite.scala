package controllers
import java.io._

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import scala.io.Source
/**
  * Created by ved on 11/8/16.
  */
object FileRead {



    def main(args:Array[String]) {

      println(Source.fromFile("/home/ved/vedproject/google-drive-scala/app/resourse/employee.txt")) // returns scala.io.BufferedSource non-empty iterator instance

      val s1 = Source.fromFile("/home/ved/vedproject/google-drive-scala/app/resourse/employee.txt").mkString; //returns the file data as String
      println(s1)


      val writer = new PrintWriter(new File("/home/ved/vedproject/google-drive-scala/app/resourse/Write.txt"))

      writer.write(s1)
      writer.close()

      //Source.fromFile("Write.txt").foreach { x => print(x) }


      //splitting String data with white space and calculating the number of occurrence of each word in the file
      val counts = s1.split("\\s+").groupBy(x=>x).mapValues(x=>x.length)

      println(counts)

      println("Count of JournalDev word:"+counts("JournalDev"))

    }

  }