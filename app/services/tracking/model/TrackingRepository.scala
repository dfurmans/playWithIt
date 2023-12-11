package services.tracking.model

import cats.Monad
import play.api.cache.SyncCacheApi
import repository.InMemoryRepository
import cats.implicits._

trait TrackingRepository[M[_]] extends InMemoryRepository[String, Tracking, M] {
  def createOrUpdate(toCreateOrUpdate: Tracking): M[Tracking]
}
final class TrackingRepositoryInCacheImpl[M[_]: Monad](cache: SyncCacheApi) extends TrackingRepository[M] {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

  override def save(aTracking: Tracking): Tracking = {
    import scala.concurrent.duration.Duration
    import scala.concurrent.duration
    cache.set(
      Tracking.getClass.getSimpleName+aTracking.reference,
      aTracking,
      Duration(cacheInvalidateInterval, duration.DAYS)
    )
    aTracking
  }
  override def find(refNumber: String): M[Option[Tracking]] = {
    cache.get[Tracking](Tracking.getClass.getSimpleName+refNumber).pure[M]
  }

  override def createOrUpdate(toCreateOrUpdate: Tracking): M[Tracking] = {
    lazy val removeAndCreate: Tracking => Tracking = aTracking => {
      cache.remove(aTracking.reference)
      save(toCreateOrUpdate)
    }
    find(toCreateOrUpdate.reference).map{
      case Some(a) if a.equals(toCreateOrUpdate) => a
      case Some(a) => removeAndCreate(a)
      case _    => save(toCreateOrUpdate)
    }
  }
}


