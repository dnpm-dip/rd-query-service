package de.dnpm.dip.rd.query.impl 



import java.io.File
import scala.concurrent.Future
import scala.util.{
  Try,
  Failure
}
import cats.{
  Applicative,
  Id,
  Monad
}
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
import de.dnpm.dip.service.Connector
import de.dnpm.dip.connector.{
  FakeConnector,
  HttpConnector
}
import de.dnpm.dip.service.query.{
  BaseQueryService,
  Filters,
  LocalDB,
  Query,
  QueryCache,
  BaseQueryCache,
  PatientFilter,
  PreparedQueryDB,
  InMemPreparedQueryDB
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.icd.ICD10GM
import de.dnpm.dip.rd.model.{
  HPO,
  HPOTerm,
  OMIM,
  Orphanet,
  RDDiagnosis,
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

  private val cache =
    new BaseQueryCache[RDQueryCriteria,RDFilters,RDResultSet,RDPatientRecord]


  private lazy val connector =
    System.getProperty(HttpConnector.Type.property,"broker") match {
      case HttpConnector.Type(t) =>
        HttpConnector(
          t,
          "/api/rd/peer2peer/",
          PartialFunction.empty
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
  val cache: QueryCache[RDQueryCriteria,RDFilters,RDResultSet,RDPatientRecord]
)
extends BaseQueryService[Future,RDConfig]
with RDQueryService
with Completers
{


//  override val ResultSetFrom =
//    new RDResultSetImpl(_,_)
  override def ResultSetFrom(
    query: Query[RDQueryCriteria,RDFilters],
    results: Seq[Query.Match[RDPatientRecord,RDQueryCriteria]]
  ) =
    new RDResultSetImpl(query.id,results)


  override def DefaultFilter(
    results: Seq[Snapshot[RDPatientRecord]]
  ): RDFilters = {

    val records =
      results.map(_.data)

    RDFilters(
      PatientFilter.on(records),
      HPOFilter(
        Option(
          records.flatMap(_.hpoTerms.map(_.value).toList)
            .toSet
        )
      ),
      DiagnosisFilter(
        Option(
          records.flatMap(_.diagnosis.categories.toList)
            .toSet
        )
      )
    )
  }


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
