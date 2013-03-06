package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._

class DataGenerateService {
  def generate(): Unit = {
    val (titles, idToIndexMap) = loadPages()
    println(titles.toList)
    println(idToIndexMap)
  }

  def loadPages(): (Array[String], Map[Int, Int]) = {
    Database.forURL("jdbc:mysql://localhost/wikipedia", driver = "com.mysql.jdbc.Driver", user = "user") withSession { implicit session: Session =>
      val query = for {
        page <- Pages if page.namespace === 0L && page.isRedirect === false
      } yield (page.id, page.title)
      val pageIdTitleList: List[(Int, String)] = query.take(10).list.map {
        case (id, title) => (id, new String(title, "UTF-8"))
      }
      val titles: Array[String] = pageIdTitleList.map {
        case (id, title) => title
      }.toArray[String].sorted
      val idToIndexMap: Map[Int, Int] = pageIdTitleList.map {
        case (id, title) => (id, java.util.Arrays.binarySearch(titles.asInstanceOf[Array[Object]], title))
      }.toMap[Int, Int]
      (titles, idToIndexMap)
    }
  }
}