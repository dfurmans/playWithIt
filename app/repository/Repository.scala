package repository

import com.typesafe.config.ConfigFactory

trait Repository[ID, A, M[_]]{
  def save(toSave: A): A
  def find(refNumber: ID) : M[Option[A]]
}

// a base for all Repo in the Memo
trait InMemoryRepository[ID, A, M[_]] extends Repository[ID, A, M] {
   val cacheInvalidateInterval: Int = ConfigFactory.load(getClass.getClassLoader).getInt("cache.timeout")
}