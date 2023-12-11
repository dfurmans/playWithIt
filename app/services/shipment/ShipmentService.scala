package services.shipment

import cats.Monad
import play.api.cache.SyncCacheApi
import services.shipment.model.{Shipment, ShipmentRepository, ShipmentRepositoryInSyncCacheImpl}

class ShipmentService[M[_] : Monad](cache: SyncCacheApi) extends ShipmentT[M]{

  //The same higher kinded type as the service works
  private val shipmentRepository: ShipmentRepository[M] = new ShipmentRepositoryInSyncCacheImpl[M](cache)

  override def register(aShipment: Shipment): Shipment = {
    shipmentRepository.save(aShipment)
  }

  override def check(aReference: String): M[Option[Shipment]] = {
    shipmentRepository.find(aReference)
  }
}
