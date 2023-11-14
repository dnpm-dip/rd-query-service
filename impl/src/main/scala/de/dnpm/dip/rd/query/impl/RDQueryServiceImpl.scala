package de.dnpm.dip.rd.query.impl 



import java.io.File
import scala.concurrent.Future
import scala.util.{
  Try,
  Failure
}
import cats.Monad
import de.dnpm.dip.util.{
  Completer,
  Logging
}
import de.dnpm.dip.model.{
  ClosedInterval,
  Site,
  Snapshot,
  Patient
}
import de.dnpm.dip.service.query.{
  BaseQueryService,
  Connector,
  Data,
  Query,
  QueryCache,
  BaseQueryCache,
  PatientFilter,
  InMemLocalDB,
  PreparedQueryDB,
  InMemPreparedQueryDB,
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.rd.model.{
  HPO,
  Orphanet,
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api._
//import de.dnpm.dip.connector.BrokerConnector
import de.dnpm.dip.connector.FakeConnector



class RDQueryServiceProviderImpl
extends RDQueryServiceProvider
{

  override def getInstance: RDQueryService =
    return RDQueryServiceImpl.instance

}


object RDQueryServiceImpl extends Logging
{

  private val cache =
    new BaseQueryCache[RDQueryCriteria,RDFilters,RDResultSet,RDPatientRecord]

  import scala.concurrent.ExecutionContext.Implicits.global

  private val connector =
    FakeConnector[Future]
//    BrokerConnector(
//      "/api/rd/peer2peer/",
//      PartialFunction.empty
//    )


  import de.ekut.tbi.generators.Gen
  import de.dnpm.dip.rd.gens.Generators._
  import scala.util.chaining._
  import scala.util.Random

  private val db =
    new InMemLocalDB[Future,Monad,RDQueryCriteria,RDPatientRecord](
      RDQueryCriteriaOps.criteriaMatcher(strict = true)
    )
    with RDLocalDB

/*
  private val sysProp =
    "dnpm.dip.rd.query.datadir"

  private val db =
    new FSBackedRDLocalDB(
      Try {
        Option(System.getProperty(sysProp)).get
      }
      .recoverWith {
        case t =>
          log.error(s"Please define system property '$sysProp' for RD query data persistence directory")
          Failure(t)
      }
      .map(new File(_))
      .get
    )
*/


  private[impl] lazy val instance =
    new RDQueryServiceImpl(
      new InMemPreparedQueryDB[Future,Monad,RDQueryCriteria],
      db,
      connector,
      cache
    )

  Try(
    Option(System.getProperty("dnpm.dip.rd.query.data.generate")).get
  )
  .map(_.toInt)
  .foreach {
    n =>
      implicit val rnd: Random = new Random
      for (i <- 0 until n){
        instance ! Data.Save(Gen.of[RDPatientRecord].next)
      }
  }
    
}


class RDQueryServiceImpl
(
  val preparedQueryDB: PreparedQueryDB[Future,Monad[Future],RDQueryCriteria,String],
  val db: RDLocalDB,
  val connector: Connector[Future,Monad[Future]],
  val cache: QueryCache[RDQueryCriteria,RDFilters,RDResultSet,RDPatientRecord]
)
extends BaseQueryService[
  Future,
  RDConfig
]
with RDQueryService
with Completers
{

  override val ResultSetFrom =
    new RDResultSetImpl(_,_)


  override def DefaultFilters(
    rs: Seq[Snapshot[RDPatientRecord]]
  ): RDFilters =
    RDFilters(
      PatientFilter.on(rs.map(_.data.patient))
    )


  override val localSite: Coding[Site] =
    connector.localSite
      
    
  override implicit val hpOntology: CodeSystem[HPO] =
    HPO.Ontology
      .getInstance[cats.Id]
      .get
      .latest

  override implicit val ordo: CodeSystem[Orphanet] =
    Orphanet.Ordo
      .getInstance[cats.Id]
      .get
      .latest


  override implicit val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest

        
  import Completer.syntax._    

  //TODO: Complete codings, etc
  override val preprocess: RDPatientRecord => RDPatientRecord =
    _.complete


}
