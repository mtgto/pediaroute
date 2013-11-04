import play.api._
import play.api.mvc._

import org.joda.time._
import scala.concurrent.Future

object Global extends WithFilters(AccessLog) {
  override def onStart(app: Application) {
    Logger.info("Application has started")
  }  
  
  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }
}

object AccessLog extends Filter {
  override def apply(next: RequestHeader => Future[SimpleResult])(request: RequestHeader): Future[SimpleResult] = {
    val result = next(request)
    play.Logger.info("%s - guest [%s] \"%s %s %s\"".format(
      request.remoteAddress, new DateTime(), request.method, request.uri, request.version)
    )
    result
  }
}