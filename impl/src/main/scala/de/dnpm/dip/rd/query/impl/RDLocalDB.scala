package de.dnpm.dip.rd.query.impl


import java.io.File
import scala.util.Either
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
  FSBackedLocalDB
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.RDQueryCriteria
import de.dnpm.dip.util.{
  SPI,
  SPILoader
}


trait RDLocalDB extends LocalDB[
  Future,
  Monad[Future],
  RDQueryCriteria,
  RDPatientRecord
]

trait RDLocalDBSPI extends SPI[RDLocalDB]

object RDLocalDB extends SPILoader[RDLocalDBSPI]




class FSBackedRDLocalDB(
  dataDir: File
)
extends FSBackedLocalDB[
  Future,
  Monad,
  RDQueryCriteria,
  RDPatientRecord
](
  dataDir,
  "RDPatientRecord",
  RDQueryCriteriaOps.criteriaMatcher(strict = true),
)
with RDLocalDB

