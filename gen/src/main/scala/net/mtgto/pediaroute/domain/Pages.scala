package net.mtgto.pediaroute.domain

import scala.slick.driver.MySQLDriver.simple._

object Pages extends Table[(Int, Long, Array[Byte], Boolean)]("page") {
  def id = column[Int]("page_id", O.PrimaryKey) // This is the primary key column
  def namespace = column[Long]("page_namespace")
  def title = column[Array[Byte]]("page_title")
  def isRedirect = column[Boolean]("page_is_redirect")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = id ~ namespace ~ title ~ isRedirect
}