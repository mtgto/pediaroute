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
    val (forwardLinks, backwardLinks) = loadPageLinks(idToIndexMap, titleToIds)
    writeForwardLinks(forwardLinks)
    writeBackwardLinks(backwardLinks)
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

  def writeForwardLinks(links: Array[Seq[Int]]): Unit = {
    val output = Resource.fromFile("forwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      links.foreach { ary =>
        out.writeStrings(ary.map(_.toString), ",")
        out.write("\n")
      }
    }
  }

  def writeBackwardLinks(links: Array[Seq[Int]]): Unit = {
    val output = Resource.fromFile("backwardlinks.dat")
    for {
      processor <- output.outputProcessor
      out = processor.asOutput
    } {
      links.foreach { ary =>
        out.writeStrings(ary.map(_.toString), ",")
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

  def loadPageLinks(idToIndexMap: Map[Int, Int], titleToIds: Array[(String, Int)]): (Array[Seq[Int]], Array[Seq[Int]]) = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val query = for {
        pageLink <- PageLinks
        page <- Pages if pageLink.title === page.title && pageLink.namespace === 0L && page.namespace === 0L && page.isRedirect === false
      } yield (pageLink.from, page.id)
      val forwardLinks: Array[Seq[Int]] = titleToIds.map(_ => Seq.empty[Int])
      val backwardLinks: Array[Seq[Int]] = titleToIds.map(_ => Seq.empty[Int])
      for (i <- 0 until 4949; subquery = query.drop(i*10000).take(10000)) {
        subquery.foreach {
          case (fromId, toId) => {
            val fromIndex = idToIndexMap.getOrElse(fromId, -1)
            val toIndex = idToIndexMap.getOrElse(toId, -1)
            if (fromIndex >= 0 && toIndex >= 0) {
              forwardLinks(fromIndex) +:= toIndex
              backwardLinks(toIndex) +:= fromIndex
            }
          }
        }
      }
      (forwardLinks, backwardLinks)
    }
  }
}