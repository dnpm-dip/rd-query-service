package de.dnpm.dip.rd.query.impl 



import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.model.Snapshot
import de.dnpm.dip.service.query.{
  BaseQueryService,
  Connector,
  Query,
  QueryCache,
  BaseQueryCache
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api._



class RDQueryServiceProviderImpl
extends RDQueryServiceProvider
{

  override def getInstance = ???
//    return new RDQueryServiceImpl

}


object RDQueryServiceImpl
{

  lazy val cache =
    new BaseQueryCache[RDCriteria,RDFilters,RDResultSet,RDPatientRecord]


}


class RDQueryServiceImpl(
  val localDB: RDLocalDB,
  val connector: Connector[Future,Monad[Future]],
  val cache: QueryCache[RDCriteria,RDFilters,RDResultSet,RDPatientRecord]
)
extends BaseQueryService[
  Future,
  RDConfig
]{

  protected def DefaultFilters(
    rs: Seq[Snapshot[PatientRecord]]
  ): RDFilters = ???


  override val ResultSetFrom =
    new RDResultSetImpl(_,_)


  override def toPredicate(
    flts: RDFilters
  ): PatientRecord => Boolean = ???


  override val preprocess: RDPatientRecord => RDPatientRecord =
    identity



}
