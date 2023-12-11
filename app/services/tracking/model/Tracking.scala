package services.tracking.model

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json._

sealed trait TrackingStatus
object TrackingStatus {
  case object WAITING_IN_HUB extends TrackingStatus
  case object DELIVERED extends TrackingStatus
  case object UNKNOWN extends TrackingStatus

}

sealed trait Tracker
final case class Tracking(status: String,
                          parcels: Option[Int],
                          weight: Option[Int],
                          reference: String) extends Tracker

object Tracking {
  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(optionHandlers = OptionHandlers.WritesNull)
  implicit val format: Format[Tracking] = Json.format[Tracking]
}

//It should be strongly and properly typed.
final case class TrackingBusinessModel(status: TrackingStatus,
                                       parcels: Option[Int],
                                       weight: Option[Int],
                                       reference: String) extends Tracker

object TrackingBusinessModel {
  def apply(aServiceModel: Tracking): TrackingBusinessModel = {
    TrackingBusinessModel(
      status = {
        aServiceModel.status match {
          case "WAITING_IN_HUB" => TrackingStatus.WAITING_IN_HUB
          case "DELIVERED"      => TrackingStatus.DELIVERED
          case _                => TrackingStatus.UNKNOWN
        }
      },
      parcels = aServiceModel.parcels,
      weight = aServiceModel.weight,
      reference = aServiceModel.reference
    )
  }
}