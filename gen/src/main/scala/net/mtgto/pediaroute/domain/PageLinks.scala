package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._

class PageLinks(tag: Tag) extends Table[(Int, Long, Array[Byte])](tag, "pagelinks") {
  def from = column[Int]("pl_from", O.PrimaryKey) // This is the primary key column
  def namespace = column[Long]("pl_namespace")
  def title = column[Array[Byte]]("pl_title")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (from, namespace, title)
}