package de.dnpm.dip.rd.query.impl 


import scala.concurrent.Future
import cats.{
  Applicative,
  Id,
  Monad
}
import de.dnpm.dip.util.Logging
import de.dnpm.dip.service.Connector
import de.dnpm.dip.connector.{
  FakeConnector,
  HttpConnector,
  HttpMethod
}
import de.dnpm.dip.service.query.{
  BaseQueryService,
  LocalDB,
  Query,
  QueryCache,
  BaseQueryCache,
  PeerToPeerQuery,
  PatientRecordRequest,
  PreparedQueryDB,
  InMemPreparedQueryDB
}
import de.dnpm.dip.coding.{
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.icd.ICD10GM
import de.dnpm.dip.rd.model.{
  HPO,
  OMIM,
  Orphanet,
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api._



class RDQueryServiceProviderImpl
extends RDQueryServiceProvider
{

  override def getInstance: RDQueryService =
    return RDQueryServiceImpl.instance

}


object RDQueryServiceImpl extends Logging
{

  import HttpMethod._

  private val cache =
    new BaseQueryCache[RDQueryCriteria,RDResultSet,RDPatientRecord]

  private lazy val connector =
    System.getProperty(HttpConnector.Type.property,"broker") match {
      case HttpConnector.Type(t) =>
        HttpConnector(
          t,
          {
            case _: PeerToPeerQuery[_,_] =>
              (POST, "/api/rd/peer2peer/query", Map.empty)

            case _: PatientRecordRequest[_] =>
              (GET, "/api/rd/peer2peer/patient-record", Map.empty)
          }

        )

      case _ => 
        import scala.concurrent.ExecutionContext.Implicits._
        log.warn("Falling back to Fake Connector!")
        FakeConnector[Future]
    }


  private[impl] lazy val instance =
    new RDQueryServiceImpl(
      new InMemPreparedQueryDB[Future,Monad,RDQueryCriteria],
      RDLocalDB.instance,
      connector,
      cache
    )

}


class RDQueryServiceImpl
(
  val preparedQueryDB: PreparedQueryDB[Future,Monad[Future],RDQueryCriteria,String],
  val db: LocalDB[Future,Monad[Future],RDQueryCriteria,RDPatientRecord],
  val connector: Connector[Future,Monad[Future]],
  val cache: QueryCache[RDQueryCriteria,RDResultSet,RDPatientRecord]
)
extends BaseQueryService[Future,RDConfig]
with RDQueryService
with Completers
{


  override def ResultSetFrom(
    query: Query[RDQueryCriteria],
    results: Seq[Query.Match[RDPatientRecord,RDQueryCriteria]]
  ): RDResultSet =
    new RDResultSetImpl(query.id,results)


  override implicit val hpOntology: CodeSystem[HPO] =
    HPO.Ontology
      .getInstance[cats.Id]
      .get
      .latest


  override implicit val ordo: CodeSystemProvider[Orphanet,Id,Applicative[Id]] =
    Orphanet.Ordo
      .getInstance[cats.Id]
      .get


  override implicit val omim: CodeSystemProvider[OMIM,Id,Applicative[Id]] =
    OMIM.Catalog
      .getInstance[cats.Id]
      .get


  override implicit val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get

  override implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]] =
    ICD10GM.Catalogs
      .getInstance[cats.Id]
      .get

}
