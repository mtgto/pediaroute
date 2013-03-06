package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._

import java.util.Arrays

class DataGenerateService {
  def generate(): Unit = {
    val (titles, idToIndexMap) = loadPages()
    val (forwardLinks, backwardLinks) = loadPageLinks(titles, idToIndexMap)
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

      query.take(10).foreach {
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
      (forwardLinks, backwardLinks)
    }
  }
}