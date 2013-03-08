package net.mtgto.pediaroute.application

import net.mtgto.pediaroute.domain.SearchService

import collection.mutable.HashMap
import scalax.io.{Resource, Input}

object DataGenerator extends App {
  val service = new SearchService(
    HashMap("東京" -> 0, "大学" -> 1, "高尾山" -> 2, "紅葉狩り" -> 3, "文京区" -> 4, "本郷" -> 5),
    Array("東京", "大学", "高尾山", "紅葉狩り", "文京区", "本郷"),
    Array(Array(2,4), Array(), Array(0,3), Array(), Array(0,5), Array(1)),
    Array(Array(2,4), Array(5), Array(0), Array(2), Array(0), Array(4)))
  Range(0, 10).foreach { _ =>
    val query = service.getRandomQuery
    println("[%s:%s] %s".format(query.from, query.to, service.find(query.from, query.to)))
  }

  def readTitleToIds(): Array[(String, Int)] = {
    val input: Input = Resource.fromFile("title.dat")
    input.lines().map { line =>
      val (idStr, wordWithComma) = line.span(_ != ',')
      (wordWithComma.tail, idStr.toInt)
    }.toArray[(String, Int)]
  }
}