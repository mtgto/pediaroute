package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._
import scalax.io.{Resource, Output, Input, OutputConverter}
import scalax.io._
import Resource._

import java.util.Arrays

class DataGenerateService {
  def generate(): Unit = {
    val (titles, idToIndexMap) = loadPages()
    val (forwardLinks, backwardLinks) = loadPageLinks(titles, idToIndexMap)
    writeTitles(titles)
    writeForwardLinks(forwardLinks)
    writeBackwardLinks(backwardLinks)
  }

  def writeTitles(titles: Seq[String]): Unit = {
    val output: Output = Resource.fromFile("title.dat")
    output.writeStrings(titles, "\n")
  }

  def readTitles(): Array[String] = {
    val input: Input = Resource.fromFile("title.dat")
    input.lines().toArray
  }

  def writeForwardLinks(links: Array[Seq[Int]]): Unit = {
    val output = Resource.fromFile("forwardlinks.dat")
    links.foreach { ary =>
      output.writeStrings(ary.map(_.toString), ",")
      output.write("\n")
    }
  }

  def writeBackwardLinks(links: Array[Seq[Int]]): Unit = {
    val output = Resource.fromFile("backwardlinks.dat")
    links.foreach { ary =>
      output.writeStrings(ary.map(_.toString), ",")
      output.write("\n")
    }
  }

  def readForwardLinks() = {
    val input: Input = Resource.fromFile("forwardlinks.dat")
    println(input.lines().toList)
  }

  def loadPages(): (Array[String], Map[Int, Int]) = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val query = for {
        page <- Pages if page.namespace === 0L && page.isRedirect === false
      } yield (page.id, page.title)
      val pageIdTitleList: List[(Int, String)] = query/*.take(10)*/.list.map {
        case (id, title) => (id, new String(title, "UTF-8"))
      }
      val titles: Array[String] = pageIdTitleList.map {
        case (id, title) => title
      }.toArray[String].sorted
      val idToIndexMap: Map[Int, Int] = pageIdTitleList.map {
        case (id, title) => (id, Arrays.binarySearch(titles.asInstanceOf[Array[Object]], title))
      }.toMap[Int, Int]
      (titles, idToIndexMap)
    }
  }

  def loadPageLinks(titles: Array[String], idToIndexMap: Map[Int, Int]): (Array[Seq[Int]], Array[Seq[Int]]) = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val query = for {
        pageLink <- PageLinks if pageLink.namespace === 0L
      } yield (pageLink.from, pageLink.title)
      val forwardLinks: Array[Seq[Int]] = titles.map(_ => Seq.empty[Int])
      val backwardLinks: Array[Seq[Int]] = titles.map(_ => Seq.empty[Int])
      for (i <- 0 until 6534; subquery = query.drop(i*10000).take(10000)) {
        subquery.foreach {
          case (fromId, titleBytes) => {
            val title = new String(titleBytes, "UTF-8")
            val fromIndex = idToIndexMap.getOrElse(fromId, -1)
            val toIndex = Arrays.binarySearch(titles.asInstanceOf[Array[Object]], title)
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