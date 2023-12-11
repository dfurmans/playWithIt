package services.shipment.model

import play.api.libs.json.{Format, Json}

case class Parcel(weight: Int, width: Int, height: Int, length: Int)
object Parcel {
  implicit val format: Format[Parcel] = Json.format[Parcel]
}
case class Shipment(reference : String, parcels: List[Parcel])
object Shipment {
  implicit val format: Format[Shipment] = Json.format[Shipment]
}
