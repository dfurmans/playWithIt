package controllers

import play.api.cache._
import play.api.http.ContentTypes
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc._
import services.tracking.{TrackingService, TrackingT}
import services.tracking.model.Tracking

import javax.inject._
import scala.concurrent.Future

@Singleton
class TrackingController @Inject()(val cache: SyncCacheApi,
                                   val controllerComponents: ControllerComponents
                                  ) extends BaseController {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val trackingService: TrackingT[Future] = new TrackingService[Future](cache)

  def tracking: Action[JsValue] = Action.async(parse.json) { implicit request: Request[JsValue] =>
    val placeResult: JsResult[Tracking] = request.body.validate[Tracking]
    placeResult.fold(
      error  => Future.successful(BadRequest("Err :: " + error)),
      result => create(result)
    )
  }

  private def create(aTracking: Tracking): Future[Result] = {
    val createIt = trackingService.createOrUpdate(aTracking)
    createIt map (created =>
      Ok(Json.toJson(created))
    )
  }

  def find(refNumber: String): Action[AnyContent] = Action.async {
    val maybeATracking: Future[Option[Tracking]] = trackingService.findByRef(refNumber)
    val asyncResult: Future[Result] = maybeATracking map{
      case Some(aTracking) => Ok(Json.toJson(aTracking)).as(ContentTypes.JSON)
      case _               => NotFound
    }
    asyncResult
  }

}
