package de.dnpm.dip.rd.query.api


import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.util.{
  SPI,
  SPILoader
}
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  HPO,
  HGVS,
  Variant,
  RDPatientRecord
}
import de.dnpm.dip.service.query.{
  QueryService
}



trait RDQueryService extends QueryService[
  Future,
  Monad[Future],
  RDConfig,
  String
]


trait RDQueryServiceProvider extends SPI[RDQueryService]

object RDQueryService extends SPILoader[RDQueryServiceProvider]

