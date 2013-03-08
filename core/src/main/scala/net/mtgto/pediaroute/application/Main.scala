package net.mtgto.pediaroute.application

import net.mtgto.pediaroute.domain.SearchService

object DataGenerator extends App {
  val service = new SearchService
  println(service.find("東京", "大学"))
}