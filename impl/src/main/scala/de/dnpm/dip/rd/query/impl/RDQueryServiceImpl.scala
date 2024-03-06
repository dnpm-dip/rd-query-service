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
import de.dnpm.dip.service.query.{
  BaseQueryService,
  Connector,
  Filters,
  Data,
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
import de.dnpm.dip.connector.{
  FakeConnector,
  HttpConnector
}



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
    System.getProperty("dnpm.dip.connector.type","peer2peer") match {
      case HttpConnector.Type(typ) =>
        HttpConnector(
          typ,
          "/api/rd/peer2peer/",
          PartialFunction.empty
        )

      case _ => 
        import scala.concurrent.ExecutionContext.Implicits._
        log.warn("Falling back to Fake Connector!")
        FakeConnector[Future]
    }

/*
  private val db =
    new InMemLocalDB[Future,Monad,RDQueryCriteria,RDPatientRecord](
      RDQueryCriteriaOps.criteriaMatcher(strict = true)
    )
    with RDLocalDB
*/

  private[impl] lazy val instance =
    new RDQueryServiceImpl(
      new InMemPreparedQueryDB[Future,Monad,RDQueryCriteria],
      RDLocalDB.instance,
      connector,
      cache
    )

  Try(
   System.getProperty("dnpm.dip.rd.query.data.generate").toInt
  )
  .foreach {
    n =>

      import de.ekut.tbi.generators.Gen
      import de.dnpm.dip.rd.gens.Generators._
      import scala.util.chaining._
      import scala.util.Random
      import scala.concurrent.ExecutionContext.Implicits.global

      implicit val rnd: Random =
        new Random

      for (i <- 0 until n){
        instance ! Data.Save(Gen.of[RDPatientRecord].next)
      }
  }
    
}


class RDQueryServiceImpl
(
  val preparedQueryDB: PreparedQueryDB[Future,Monad[Future],RDQueryCriteria,String],
  val db: LocalDB[Future,Monad[Future],RDQueryCriteria,RDPatientRecord],
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


  import scala.language.implicitConversions

  override implicit def toPredicate(
    filter: RDFilters
  ): RDPatientRecord => Boolean = {
    record =>

      implicit def hpoFilterPredicate(f: HPOFilter): HPOTerm => Boolean =
        term =>
          f.value match {
            case Some(hpos) if hpos.nonEmpty => hpos exists (_.code == term.value.code)
            case _ => true
          }

      implicit def diagnosisFilterPredicate(f: DiagnosisFilter): RDDiagnosis => Boolean =
        diag =>
          f.category match {
             case Some(orphas) if orphas.nonEmpty => diag.categories exists (c => orphas exists (_.code == c.code))
             case _ => true
          }

      filter.patientFilter(record.patient) &&
      record.hpoTerms.exists(filter.hpoFilter) &&
      filter.diagnosisFilter(record.diagnosis)

  }


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


  override implicit val omim: CodeSystem[OMIM] =
    OMIM.Catalog
      .getInstance[cats.Id]
      .get
      .latest


  override implicit val hgnc: CodeSystem[HGNC] =
    HGNC.GeneSet
      .getInstance[cats.Id]
      .get
      .latest

  override implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]] =
    ICD10GM.Catalogs
      .getInstance[cats.Id]
      .get
  

  import Completer.syntax._    

  override val preprocess: RDPatientRecord => RDPatientRecord =
    _.complete

}
