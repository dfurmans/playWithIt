package services.shipment.model

import cats.Monad
import play.api.cache.SyncCacheApi
import repository.InMemoryRepository
import cats.implicits._

trait ShipmentRepository[M[_]] extends InMemoryRepository[String, Shipment, M]

final class ShipmentRepositoryInSyncCacheImpl[M[_]: Monad](cache: SyncCacheApi) extends ShipmentRepository[M] {
  override def save(aShipment: Shipment): Shipment = {
    import scala.concurrent.duration.Duration
    import scala.concurrent.duration
    cache.set(
      Shipment.getClass.getSimpleName + aShipment.reference,
      aShipment,
      Duration(cacheInvalidateInterval, duration.DAYS)
    )
    aShipment
  }

  override def find(refNumber: String): M[Option[Shipment]] = {
    cache.get[Shipment](Shipment.getClass.getSimpleName+refNumber).pure[M]
  }

}