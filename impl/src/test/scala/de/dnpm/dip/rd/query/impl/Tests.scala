package de.dnpm.dip.rd.query.impl


import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
import org.scalatest.EitherValues._
import org.scalatest.Inspectors._
import scala.util.Random
import scala.concurrent.Future
import de.dnpm.dip.coding.{
  Code,
  CodeSystem,
  Coding
}
import de.dnpm.dip.rd.query.api._
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.service.query.{
  BaseQueryCache,
  Data,
  Query,
  Querier,
}
import de.dnpm.dip.connector.FakeConnector
import de.ekut.tbi.generators.Gen
import play.api.libs.json.{Json,Writes}


class Tests extends AsyncFlatSpec
{

  import scala.util.chaining._
  import de.dnpm.dip.rd.gens.Generators._


  implicit val rnd: Random = new Random

  implicit val querier: Querier = Querier("Dummy-Querier-ID")

  
  val service =
    new RDQueryServiceImpl(
      new InMemRDLocalDB,
      FakeConnector[Future],
      new BaseQueryCache[RDCriteria,RDFilters,RDResultSet,RDPatientRecord]
    )


  val dataSets =
    LazyList.fill(100)(Gen.of[RDPatientRecord].next)


  // Generator for non-empty Query Criteria based on features occurring in a given dataset,
  // and thus guaranteed to always match at least this one data set
  val genCriteria: Gen[RDCriteria] =
    for {
      patRec <-
        Gen.oneOf(dataSets)

      category =
        patRec.diagnosis.category

      diagnosisCriteria =
        DiagnosisCriteria(
          Some(category),
          None
        )

      hpoCoding <-
        Gen.oneOf(
          patRec
            .hpoTerms
            .getOrElse(List.empty)
            .map(_.value)
            .distinctBy(_.code)
        )

      variant =
        patRec
          .ngsReport
          .variants
          .getOrElse(List.empty)
          .head  // safe, generated variants always non-empty

      variantCriteria =  
        VariantCriteria(
          Some(variant.gene),
          variant.cDNAChange,
          variant.gDNAChange,
          variant.proteinChange
        )
      
    } yield RDCriteria(
      Some(Set(diagnosisCriteria)),
      Some(Set(hpoCoding)),
      Some(Set(variantCriteria))
    )



  private def printJson[T: Writes](t: T) =
    t.pipe(Json.toJson(_))
     .pipe(Json.prettyPrint)
     .tap(println)




  "Importing RDPatientRecords" must "have worked" in {

    for {
      outcomes <- Future.traverse(dataSets)(service ! Data.Save(_))
    } yield forAll(outcomes){ _.isRight mustBe true }
    
  }


  val queryMode =
    Query.Mode.Local
//    CodeSystem[Query.Mode]
//      .codingWithCode(Query.Mode.Local)
//      .get


  "Query ResultSet" must "contain the total number of data sets for a query without criteria" in {

    for {
      result <-
        service ! Query.Submit(
          queryMode,
          RDCriteria(None,None,None)
        )

      query =
        result.right.value

      resultSet <-
        service.resultSet(query.id).map(_.value)

    } yield resultSet.summary.numPatients must equal (dataSets.size) 

  }


  it must "contain a non-empty list of correctly matching data sets for a query with criteria" in {

    import RDCriteriaOps._

    for {
      result <-
        service ! Query.Submit(
          queryMode,
          genCriteria.next
        )

      query =
        result.right.value

      resultSet <-
        service.resultSet(query.id)
          .map(_.value)

      patientMatches = 
        resultSet.patientMatches

      _ = patientMatches must not be empty

    } yield forAll(
        patientMatches.map(_.matchingCriteria)
      ){ 
        matches =>
          assert( (query.criteria intersect matches).nonEmpty )
      }

  }

}
