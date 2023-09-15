package de.dnpm.dip.rd.query.impl



import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.OptionValues._
import org.scalatest.EitherValues._
import org.scalatest.Inspectors._

import java.nio.file.Files.createTempDirectory
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
  Querier
}
import de.dnpm.dip.connector.FakeConnector
import de.ekut.tbi.generators.Gen
import play.api.libs.json.{Json,Writes}


class Tests extends AsyncFlatSpec
{

  import scala.util.chaining._
  import de.dnpm.dip.rd.gens.Generators._


  implicit val rnd: Random = new Random

  implicit val querier: Querier = Querier("Dummy")

  
  val service =
    new RDQueryServiceImpl(
      new FSBackedRDLocalDB(
        createTempDirectory("rd_query_test_data").toFile
      ),
      FakeConnector[Future],
      new BaseQueryCache[RDCriteria,RDFilters,RDResultSet,RDPatientRecord]
    )


  val dataSets =
    LazyList.fill(100)(Gen.of[RDPatientRecord].next)


  // Generator for Query Criteria based on features occurring in generated dataSets
  implicit val genCriteria: Gen[RDCriteria] =
    for {
      category <-
        Gen.oneOf(
          dataSets
            .map(_.diagnosis.category)
            .distinctBy(_.code)
        )

      diagnosisCriteria =
        Set(
          DiagnosisCriteria(
            Some(category),
            None
          )
        )

      hpoCoding <-
        Gen.oneOf(
          dataSets
            .flatMap(_.hpoTerms.getOrElse(List.empty))
            .map(_.value)
            .distinctBy(_.code)
        )
      
    } yield RDCriteria(
      Some(diagnosisCriteria),
      Some(Set(hpoCoding)),
      None
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


  "Submitting an empty query" must "have worked" in {

    val command =  
      Query.Submit(
        CodeSystem[Query.Mode]
          .codingWithCode(Query.Mode.Local)
          .get,
        RDCriteria(None,None,None)
      )

    for {
      result <- service ! command

      query = result.right.value

      resultSet <- service.resultSet(query.id).map(_.value)

    } yield resultSet.summary.numPatients must equal (dataSets.size) 

  }


  "Submitting a non- empty query" must "have worked" in {

    import RDCriteriaOps._


    val command =  
      Query.Submit(
        CodeSystem[Query.Mode]
          .codingWithCode(Query.Mode.Local)
          .get,
        Gen.of[RDCriteria].next
      )

    for {
      result <- service ! command

      query = result.right.value

      _ = printJson(query)

      resultSet <- service.resultSet(query.id).map(_.value)

      patientMatches = resultSet.patientMatches

      _ = printJson(patientMatches)

    } yield forAll(
        patientMatches.map(_.matchingCriteria)
      )( 
        matches =>
          assert(
            query.criteria.isEmpty || (query.criteria intersect matches).nonEmpty
          )
      )

  }

}
