package controllers

import java.io.File

/**
  * Created by ved on 12/8/16.
  */
object Folder {

  def main(args: Array[String]): Unit = {

    val path = getClass.getResource("/resourse")
    val folder = new File(path.getPath)
    if (folder.exists && folder.isDirectory)
      folder.listFiles
        .toList
        .foreach(file => println(file.getName))

  }
}