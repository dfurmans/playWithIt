package services.shipment

import services.shipment.model.Shipment

trait ShipmentT[M[_]]{
  def register(aShipment: Shipment): Shipment
  def check(aReference: String) : M[Option[Shipment]]
}
