package controllers

import play.api._
import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

object Application extends Controller {
  protected[this] val searchForm = Form(
    tuple(
      "wordFrom" -> nonEmptyText,
      "wordTo" -> nonEmptyText
    )
  )
  
  def index = Action {
    Ok(views.html.index(searchForm))
  }
  
  def search = Action { implicit request =>
    searchForm.bindFromRequest.fold(
      formWithErrors =>
        BadRequest(views.html.index(formWithErrors)),
      success => success match {
        // Either[Seq[String], String] Left: Answer, Right: Reason of failure
        case (from, to) =>
          Ok(views.html.search(from, to, new Left[Seq[String], String](Seq("ほげ", "fuga", "12345")), 123.45))
      }
    )
  }
}