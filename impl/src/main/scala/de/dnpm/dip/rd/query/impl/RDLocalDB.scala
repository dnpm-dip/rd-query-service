package de.dnpm.dip.rd.query.impl


import java.io.File
import scala.util.Try
import scala.util.chaining._
import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.model.{
  Id,
  Patient,
  Snapshot,
  Site
}
import de.dnpm.dip.service.query.{
  LocalDB,
  FSBackedLocalDB,
  InMemLocalDB
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.RDQueryCriteria
import de.dnpm.dip.util.{
  SPI,
  SPILoader
}
import de.ekut.tbi.generators.Gen
import de.dnpm.dip.rd.gens.Generators._
import de.dnpm.dip.rd.model.Completers._
import de.dnpm.dip.util.Completer.syntax._
import scala.util.chaining._
import scala.util.Random


trait RDLocalDB extends LocalDB[
  Future,
  Monad[Future],
  RDQueryCriteria,
  RDPatientRecord
]

trait RDLocalDBSPI extends SPI[RDLocalDB]

object RDLocalDB extends SPILoader[RDLocalDBSPI]
{

  private[impl] val dataGenProp =
    "dnpm.dip.rd.query.data.generate"

  private[impl] val rdDataDirProp =
    "dnpm.dip.rd.query.data.dir"

  private[impl] val dataDirProp =
    "dnpm.dip.data.dir"


  lazy val instance: LocalDB[Future,Monad[Future],RDQueryCriteria,RDPatientRecord] =
    getInstance
      .getOrElse {

        val matcher =
          RDQueryCriteriaOps.criteriaMatcher(strict = true)

        Try(
          System.getProperty(dataGenProp).toInt
        )
        .collect {
          case n if n >= 0 =>
            log.warn(s"Random data generation activated, using in-memory DB only!")
            new InMemLocalDB[Future,Monad,RDQueryCriteria,RDPatientRecord](matcher)
              .tap {
                db =>

                import scala.concurrent.ExecutionContext.Implicits.global

                implicit val rnd: Random = new Random

                for (i <- 0 until n){
                  db.save(Gen.of[RDPatientRecord].next.complete)
                }
              }

        }
        .getOrElse(
          Option(System.getProperty(rdDataDirProp))
            .orElse(
              Option(System.getProperty(dataDirProp)).map(dir => s"$dir/rd_data")
            ) match {

            case Some(dir) =>
              new FSBackedLocalDB[Future,Monad,RDQueryCriteria,RDPatientRecord](
                new File(dir),
                matcher
              )

            case None =>
              val msg =
                s"System property $dataDirProp or $rdDataDirProp for the data storage directory is undefined, but random data generation is also not activated!"
                 .tap(log.error)
              throw new IllegalStateException(msg)
          }
        )

      }

}
