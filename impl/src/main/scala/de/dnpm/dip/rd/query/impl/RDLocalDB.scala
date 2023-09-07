package de.dnpm.dip.rd.query.impl


import scala.util.Either
import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.model.{
  Id,
  Patient,
  Snapshot,
  Site
}
import de.dnpm.dip.service.query.LocalDB
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.RDCriteria
import de.dnpm.dip.util.{
  SPI,
  SPILoader
}


trait RDLocalDB extends LocalDB[
  Future,
  Monad[Future],
  RDCriteria,
  RDPatientRecord
]

trait RDLocalDBSPI extends SPI[RDLocalDB]

object RDLocalDB extends SPILoader[RDLocalDBSPI]
