package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._
import scalax.file.Path
import scalax.io.{Resource, Output, Input, OutputConverter}
import Resource._

import java.util.Arrays

class DataGenerateService {
  def generate(): Unit = {
    //val titleToIds: Array[(String, Int)] = Array(("AAA", 123), ("BBB", 45), ("CCC", 678))
    val titleToIdsPath = Path("title.dat")
    val titleToIds: Array[(String, Int)] =
      if (titleToIdsPath.exists) {
        readTitleToIds
      } else {
        val load = loadPages()
        writeTitleToIds(load)
        load
      }
    // id to index
    val idToIndexMap: Map[Int, Int] = titleToIds.map(_._2).zipWithIndex.toMap
    val links: Array[(Int, Int)] = loadPageLinks(idToIndexMap)
    println("num of links = " + links.length)
    writeForwardLinks(titleToIds.length, links.sortBy(_._1))
    writeBackwardLinks(titleToIds.length, links.sortBy(_._2))
  }

  def writeTitleToIds(titleToIds: Array[(String, Int)]): Unit = {
    val output: Output = Resource.fromFile("title.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      titleToIds.foreach {
        case (title, id) =>
          out.write(id.toString + "," + title + "\n")
      }
    }
  }

  def readTitleToIds(): Array[(String, Int)] = {
    val input: Input = Resource.fromFile("title.dat")
    input.lines().map { line =>
      val (idStr, wordWithComma) = line.span(_ != ',')
      (wordWithComma.tail, idStr.toInt)
    }.toArray[(String, Int)]
  }

  def writeForwardLinks(indexCount: Int, links: Array[(Int, Int)]): Unit = {
    val output = Resource.fromFile("forwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      var row = 0
      for (index <- 0 until indexCount) {
        var colIndex = 0
        for (i <- row until links.length if links(i)._1 == index) {
          if (colIndex != 0) {
            out.write(",")
          }
          out.write(links(i)._2.toString)
          colIndex += 1
          row += 1
        }
        out.write("\n")
      }
    }
  }

  def writeBackwardLinks(indexCount: Int, links: Array[(Int, Int)]): Unit = {
    val output = Resource.fromFile("backwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      var row = 0
      for (index <- 0 until indexCount) {
        var colIndex = 0
        for (i <- row until links.length if links(i)._2 == index) {
          if (colIndex != 0) {
            out.write(",")
          }
          out.write(links(i)._1.toString)
          colIndex += 1
          row += 1
        }
        out.write("\n")
      }
    }
  }

  def readForwardLinks() = {
    val input: Input = Resource.fromFile("forwardlinks.dat")
    println(input.lines().toList)
  }

  def loadPages(): (Array[(String, Int)]) = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val query = for {
        page <- Pages if page.namespace === 0L && page.isRedirect === false
      } yield (page.id, page.title)
      query.list.map {
        case (id, title) => (new String(title, "UTF-8"), id)
      }.toArray[(String, Int)].sortBy(_._1)
    }
  }

  def loadPageLinks(idToIndexMap: Map[Int, Int]): Array[(Int, Int)] = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val rowCount = 49486640
      val perRowCount = 10000000
      val links = new Array[(Int, Int)](rowCount)
      val query = for {
        pageLink <- PageLinks
        page <- Pages if pageLink.title === page.title && pageLink.namespace === 0L && page.namespace === 0L && page.isRedirect === false
      } yield (pageLink.from, page.id)
      var index = 0
      for (i <- 0 until rowCount by perRowCount; subquery = query.drop(i).take(perRowCount)) {
        subquery.foreach {
          case (fromId, toId) => {
            val fromIndex = idToIndexMap.getOrElse(fromId, -1)
            val toIndex = idToIndexMap.getOrElse(toId, -1)
            if (fromIndex >= 0 && toIndex >= 0) {
              links(index) = (fromIndex, toIndex)
              index += 1
            }
          }
        }
      }
      links.take(index)
    }
  }
}