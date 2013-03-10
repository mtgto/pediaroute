package net.mtgto.pediaroute.application

import net.mtgto.pediaroute.domain.SearchService

import collection.mutable.HashMap
import scalax.io.{Resource, Input}

object DataGenerator extends App {
  val titleIdMap = readTitles()
  val titleMap = titleIdMap.zipWithIndex.toMap
  val sameTitleMap = readSameTitleMap
  val links = readForwardLinks(titleIdMap.length)
  val revLinks = readBackwardLinks(titleIdMap.length)
  val service = new SearchService(
    titleMap, titleIdMap, sameTitleMap, links, revLinks
  )
  Range(0, 100).foreach { _ =>
    val query = service.getRandomQuery
    println("[%s:%s] %s".format(query.from, query.to, service.find(query.from, query.to)))
  }

  def readTitles(): Array[String] = {
    val input: Input = Resource.fromFile("title.dat")
    input.lines().map { line =>
      line.dropWhile(_ != ',').tail
    }.toArray[String]
  }

  def readSameTitleMap(): Map[String, Array[Int]] = {
    val input: Input = Resource.fromFile("sametitle.dat")
    val lines: Array[String] = input.lines().toArray
    (for (i <- 0 until lines.length by 2) yield (lines(i), lines(i+1).split(",").map(_.toInt))).toMap
  }

  def readForwardLinks(pageCount: Int): Array[Array[Int]] = {
    val links: Array[Array[Int]] = new Array[Array[Int]](pageCount)
    val input: Input = Resource.fromFile("forwardlinks.dat")
    var i = 0
    input.lines().foreach { line =>
      links(i) = if (line.length == 0) Array.empty[Int] else line.split(",").map(_.toInt)
      i += 1
    }
    for (j <- i until pageCount) links(j) = Array.empty[Int]
    links
  }

  def readBackwardLinks(pageCount: Int): Array[Array[Int]] = {
    val links: Array[Array[Int]] = new Array[Array[Int]](pageCount)
    val input: Input = Resource.fromFile("backwardlinks.dat")
    var i = 0
    input.lines().foreach { line =>
      links(i) = if (line.length == 0) Array.empty[Int] else line.split(",").map(_.toInt)
      i += 1
    }
    for (j <- i until pageCount) links(j) = Array.empty[Int]
    links
  }
}