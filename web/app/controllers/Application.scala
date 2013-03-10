package controllers

import net.mtgto.pediaroute.domain.{SearchService, Query}

import play.api._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._

import concurrent.Future

import scalax.io.{Resource, Input}

object Application extends Controller {
  private val titleFilePath = getConfiguration("pediaroute.title_file")

  private val sameTitleFilePath = getConfiguration("pediaroute.same_title_file")

  private val forwardLinkFilePath = getConfiguration("pediaroute.forward_link_file")

  private val backwardLinkFilePath = getConfiguration("pediaroute.backward_link_file")

  private val titles: Array[String] = readTitles(titleFilePath)

  private val titleMap: Map[String, Int] = titles.zipWithIndex.toMap

  private val sameTitleMap: Map[String, Array[Int]] = readSameTitleMap(sameTitleFilePath)

  private val forwardLinks: Array[Array[Int]] = readForwardLinks(forwardLinkFilePath, titles.length)

  private val backwardLinks: Array[Array[Int]] = readBackwardLinks(backwardLinkFilePath, titles.length)

  protected[this] val searchService: SearchService = new SearchService(titleMap, titles, sameTitleMap, forwardLinks, backwardLinks)

  protected[this] val searchForm = Form(
    tuple(
      "wordFrom" -> nonEmptyText,
      "wordTo" -> nonEmptyText
    )
  )
  
  def index = Action {
    Ok(views.html.index(searchForm))
  }
  
  def search(wordFrom: String, wordTo: String) = LoggingAction { implicit request =>
    if (!searchService.isTitleExists(wordFrom)) {
      Ok(views.html.search(wordFrom, wordTo, new Right(wordFrom + "というページがないみたい"), 0.0))
    } else if (!searchService.isTitleExists(wordTo)) {
      Ok(views.html.search(wordFrom, wordTo, new Right(wordTo + "というページがないみたい"), 0.0))
    } else {
      val startTime = System.currentTimeMillis
      val futureQuery: Future[Option[Query]] = Future {
        searchService.find(wordFrom, wordTo)
      }
      Async {
        futureQuery.map { query =>
          val result: Either[Seq[String], String] = query match {
            case Some(query) => new Left(query.way)
            case _ => new Right("6回のリンクじゃ見つからなかった…ごめんね！")
          }
          val endTime = System.currentTimeMillis
          Ok(views.html.search(wordFrom, wordTo, result, (endTime-startTime).toDouble/1000))
        }
      }
    }
  }

  def random = Action {
    val query = searchService.getRandomQuery
    Ok(Json.arr(query.from, query.to))
  }

  private def getConfiguration(name: String): String = Play.current.configuration.getString(name).get

  protected[this] def readTitles(filePath: String): Array[String] = {
    val input: Input = Resource.fromFile(filePath)
    input.lines().map { line =>
      line.dropWhile(_ != ',').tail
    }.toArray[String]
  }

  protected[this] def readSameTitleMap(filePath: String): Map[String, Array[Int]] = {
    val input: Input = Resource.fromFile(filePath)
    val lines: Array[String] = input.lines().toArray
    (for (i <- 0 until lines.length by 2) yield (lines(i), lines(i+1).split(",").map(_.toInt))).toMap
  }

  protected[this] def readForwardLinks(filePath: String, pageCount: Int): Array[Array[Int]] = {
    val links: Array[Array[Int]] = new Array[Array[Int]](pageCount)
    val input: Input = Resource.fromFile(filePath)
    var i = 0
    input.lines().foreach { line =>
      links(i) = if (line.length == 0) Array.empty[Int] else line.split(",").map(_.toInt)
      i += 1
    }
    for (j <- i until pageCount) links(j) = Array.empty[Int]
    links
  }

  protected[this] def readBackwardLinks(filePath: String, pageCount: Int): Array[Array[Int]] = {
    val links: Array[Array[Int]] = new Array[Array[Int]](pageCount)
    val input: Input = Resource.fromFile(filePath)
    var i = 0
    input.lines().foreach { line =>
      links(i) = if (line.length == 0) Array.empty[Int] else line.split(",").map(_.toInt)
      i += 1
    }
    for (j <- i until pageCount) links(j) = Array.empty[Int]
    links
  }
}