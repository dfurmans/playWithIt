package services.tracking

import cats.Monad
import play.api.cache.SyncCacheApi
import services.tracking.model.{Tracking, TrackingRepository, TrackingRepositoryInCacheImpl}

class TrackingService[M[_]: Monad](cache: SyncCacheApi) extends TrackingT[M]{

  // The same higher kinded type as for the service layer
  private val trackingRepository: TrackingRepository[M] = new TrackingRepositoryInCacheImpl[M](cache)

  override def createOrUpdate(aTracking: Tracking): M[Tracking] =
    trackingRepository.createOrUpdate(aTracking)

  override def findByRef(refNumber: String): M[Option[Tracking]] = {
    trackingRepository.find(refNumber)
  }
}
