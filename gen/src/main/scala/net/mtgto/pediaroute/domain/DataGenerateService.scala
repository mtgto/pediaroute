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
    val links: Array[Long] = loadPageLinks(idToIndexMap)
    println("num of links = " + links.length)
    writeForwardLinks(titleToIds.length, links.sortBy(_ % 4294967296L))
    writeBackwardLinks(titleToIds.length, links.sortBy(_ / 4294967296L))
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

  def writeForwardLinks(indexCount: Int, links: Array[Long]): Unit = {
    val output = Resource.fromFile("forwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      var lastIndex = 0
      links.foreach { link =>
        val fromIndex = (link % 4294967296L).toInt
        val toIndex = (link / 4294967296L).toInt
        if (lastIndex < fromIndex) {
          for (i <- lastIndex until fromIndex) {
            out.write("\n")
          }
          lastIndex = fromIndex
        } else {
          out.write(",")
        }
        out.write(toIndex.toString)
      }
    }
  }

  def writeBackwardLinks(indexCount: Int, links: Array[Long]): Unit = {
    val output = Resource.fromFile("backwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      var lastIndex = 0
      links.foreach { link =>
        val fromIndex = (link % 4294967296L).toInt
        val toIndex = (link / 4294967296L).toInt
        if (lastIndex < toIndex) {
          for (i <- lastIndex until toIndex) {
            out.write("\n")
          }
          lastIndex = toIndex
        } else {
          out.write(",")
        }
        out.write(fromIndex.toString)
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

  def loadPageLinks(idToIndexMap: Map[Int, Int]): Array[Long] = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val rowCount = 49486640
      val perRowCount = 10000000
      val links = new Array[Long](rowCount)
      val query = for {
        pageLink <- PageLinks
        page <- Pages if pageLink.title === page.title && pageLink.namespace === 0L && page.namespace === 0L && page.isRedirect === false
      } yield (pageLink.from, page.id)
      var index = 0
      for (i <- 0 until rowCount by perRowCount; subquery = query.drop(i).take(perRowCount)) {
        println(i)
        subquery.foreach {
          case (fromId, toId) => {
            val fromIndex = idToIndexMap.getOrElse(fromId, -1)
            val toIndex = idToIndexMap.getOrElse(toId, -1)
            if (fromIndex >= 0 && toIndex >= 0) {
              links(index) = fromIndex + toIndex.toLong * 4294967296L
              index += 1
            }
          }
        }
      }
      links.take(index)
    }
  }
}