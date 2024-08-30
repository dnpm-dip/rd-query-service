package de.dnpm.dip.rd.query.impl


import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
import org.scalatest.EitherValues._
import org.scalatest.Inspectors._
import scala.util.Random
import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.model.Site
import de.dnpm.dip.coding.{
  Code,
  CodeSystem,
  Coding
}
import de.dnpm.dip.rd.query.api._
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.model.Completers._
import de.dnpm.dip.service.query.{
  BaseQueryCache,
  Query,
  Querier,
  PreparedQuery,
  PreparedQueryDB,
  InMemPreparedQueryDB
}
import de.dnpm.dip.service.query.QueryService.Save
import de.dnpm.dip.connector.{
  FakeConnector,
  HttpConnector
}
import de.ekut.tbi.generators.Gen
import play.api.libs.json.{
  Json,
  Writes
}


class Tests extends AsyncFlatSpec
{

  import scala.util.chaining._
  import de.dnpm.dip.rd.gens.Generators._
  import de.dnpm.dip.util.Completer.syntax._


  System.setProperty(Site.property,"UKx:Musterlingen")
  System.setProperty(HttpConnector.Type.property,"fake")
  System.setProperty(RDLocalDB.dataGenProp,"0")


  implicit val rnd: Random =
    new Random

  implicit val querier: Querier =
    Querier("Dummy-Querier-ID")


  val serviceTry =
    RDQueryService.getInstance

  lazy val service = serviceTry.get



  val dataSets =
    LazyList.fill(100)(Gen.of[RDPatientRecord].next)
      .map(_.complete)


  // Generator for non-empty Query Criteria based on features occurring in a given dataset,
  // and thus guaranteed to always match at least this one data set
  val genCriteria: Gen[RDQueryCriteria] =
    for {
      patRec <-
        Gen.oneOf(dataSets)

      category =
        patRec.diagnosis
          .categories
          .head
          .copy(display = None)  // Undefine display value to test whether Criteria completion works

      hpoCoding <-
        Gen.oneOf(
          patRec
            .hpoTerms
            .map(_.value)
            .toList
            .distinctBy(_.code)
        )
        .map(_.copy(display = None)) // Undefine display value to test whether Criteria completion works

      variant =
        patRec
          .getNgsReports
          .head 
          .variants
          .head // Safe: generated variant lists always non-empty

      variantCriteria =  
        VariantCriteria(
          variant.genes.flatMap(_.headOption),
          variant.cDNAChange,
          variant.gDNAChange,
          variant.proteinChange,
        )
      
    } yield RDQueryCriteria(
      Some(Set(category)),
      Some(Set(hpoCoding)),
      Some(Set(variantCriteria))
    )



  private def printJson[T: Writes](t: T) =
    t.pipe(Json.toJson(_))
     .pipe(Json.prettyPrint)
     .tap(println)


  "SPI" must "have worked" in {
    serviceTry.isSuccess mustBe true
  }


  "Importing RDPatientRecords" must "have worked" in {

    for {
      outcomes <-
        Future.traverse(dataSets)(service ! Save(_))
    } yield all (outcomes.map(_.isRight)) mustBe true 
    
  }


  val queryMode =
    Coding(Query.Mode.Local)


  "Query ResultSet" must "contain the total number of data sets for a query without criteria" in {

    for {
      result <-
        service ! Query.Submit(
          queryMode,
          None,
          None
        )

      query = result.value

      resultSet <-
        service.resultSet(query.id).map(_.value)

    } yield resultSet.demographics(RDFilters.empty).patientCount must equal (dataSets.size) 

  }


  it must "contain a non-empty list of correctly matching data sets for a query with criteria" in {

    import RDQueryCriteriaOps._

    for {
      result <-
        service ! Query.Submit(
          queryMode,
          None,
          Some(genCriteria.next)
        )

      query = result.value

      queryCriteria =
        query.criteria.value

      resultSet <-
        service.resultSet(query.id)
          .map(_.value)

      patientMatches = 
        resultSet.patientMatches(RDFilters.empty)

      _ = all (queryCriteria.diagnoses.value.map(_.display)) must be (defined)  
      _ = all (queryCriteria.diagnoses.value.map(_.version)) must be (defined)  

      _ = all (queryCriteria.hpoTerms.value.map(_.display)) must be (defined)  
      _ = all (queryCriteria.hpoTerms.value.map(_.version)) must be (defined)  

      _ = patientMatches must not be empty

      matchingCriteria =
        patientMatches.map(_.matchingCriteria)

      _ = all (matchingCriteria) must be (defined)


    } yield forAll(
        matchingCriteria
      ){ 
        matches =>
          assert( (queryCriteria intersect matches.value).nonEmpty )
      }

  }


  "PreparedQuery" must "have been successfully created" in {

    for {
      result <-
        service ! PreparedQuery.Create("Dummy Prepared Query",genCriteria.next)

    } yield result.isRight mustBe true 

  }

  it must "have been successfully retrieved" in {

    for {
      result <-
        service ? PreparedQuery.Filter(Some(querier))

      _ = result must not be empty 

      query <- 
        service ? result.head.id

    } yield query must be (defined)

  }


}
