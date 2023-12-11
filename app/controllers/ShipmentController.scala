package controllers

import events.{EmptyEvent, EventDispatcher, EventIssue, TrackingCase, TrackingEvent}
import play.api.libs.json.{JsResult, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, PlayBodyParsers, Request}

import javax.inject._
import play.api.cache._
import play.api.http.ContentTypes
import services.shipment.model.Shipment
import services.shipment.{ShipmentService, ShipmentT}
import services.tracking.model.TrackingBusinessModel
import services.tracking.{TrackingService, TrackingT}
import cats.effect.IO

import scala.collection.immutable.Queue
import scala.concurrent.Future
import scala.util.{Failure, Success}

@Singleton
class ShipmentController @Inject()(val controllerComponents: ControllerComponents,
                                   val cache: SyncCacheApi,
                                   val parser: PlayBodyParsers)
  extends BaseController {

  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  val shipmentService: ShipmentT[Future] = new ShipmentService[Future](cache)
  val trackingService: TrackingT[Future] = new TrackingService[Future](cache)

  def register: Action[JsValue] = Action(parse.json) { implicit request: Request[JsValue] =>
    val placeResult: JsResult[Shipment] = request.body.validate[Shipment]
    placeResult.fold(
      error  => BadRequest("Err :: " + error),
      result => {
        val aShipment: Shipment = shipmentService.register(result)
        val eventData = maybeDispatchEventForAShipment(aShipment.reference)
        val eventsReadyToDispatch: IO[Unit] = prepareIO(eventData)(aShipment.reference)
        // !!!side effects as a print event to the console!!!
        dispatchEvents(eventsReadyToDispatch)
        Ok(Json.toJson(aShipment))
      }
    )
  }

  private lazy val maybeDispatchEventForAShipment: String => Future[Option[Queue[EventIssue]]] = aReferenceNumber => {
    import cats.data.OptionT
    import cats.implicits._

    val fShipment = shipmentService.check(aReferenceNumber)
    val fTracking = trackingService.findByRef(aReferenceNumber)
    val evaluateEvents = for {
      aTrackingEntry <- OptionT(fTracking)
      aShipment <- OptionT(fShipment)
    } yield {
      EventDispatcher.BusinessRules.standardEventIssueDispatcherStrategy.performCriterionEvaluation(
        aShipment, TrackingBusinessModel.apply(aTrackingEntry))
    }
    evaluateEvents.value
  }

  lazy val prepareIO: Future[Option[Queue[EventIssue]]] => String => IO[Unit] = maybeFEvent => aReferenceNumber => {
    val callbackBody = maybeFEvent onComplete {
      case Success(maybeListOfEvents) => maybeListOfEvents match {
        case Some(aListOfEvents) =>
          aListOfEvents.foreach {
            case ourEvent@TrackingEvent(_, _) => println(TrackingEvent.renderEventAsString(ourEvent))
            case EmptyEvent                   => ""
          }
        case None =>
          // cause for-comprehension is a fast circuit breaker in case issue an NOT_FOUND event
          println(
            TrackingEvent.renderEventAsString(
              TrackingEvent(reference = aReferenceNumber, status = TrackingCase.NOT_FOUND)
            )
          )
      }
      case Failure(t) => "An error has occurred: " + t.getMessage
    }

    wrapIntoIO(callbackBody)
  }

  def find(refNumber: String): Action[AnyContent] = Action.async {

    val maybeAShipment: Future[Option[Shipment]] = shipmentService.check(refNumber)
    maybeAShipment.map {
      case Some(aShipment) => Ok(Json.toJson(aShipment)).as(ContentTypes.JSON)
      case _               => NotFound
    }
  }

  private def dispatchEvents(io: IO[Unit]): Unit = io.unsafeRunSync()

  private def wrapIntoIO(body: => Unit): IO[Unit] = IO {
    body
  }

}