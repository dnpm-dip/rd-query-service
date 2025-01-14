package de.dnpm.dip.rd.query.impl


import java.io.File
import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.service.query.{
  PreparedQueryDB,
  FSBackedPreparedQueryDB,
  InMemPreparedQueryDB
}
import de.dnpm.dip.rd.query.api.RDQueryCriteria
import de.dnpm.dip.util.Logging


trait RDPreparedQueryDB extends PreparedQueryDB[
  Future,
  Monad[Future],
  RDQueryCriteria,
  String
]

object RDPreparedQueryDB extends Logging
{

  private[impl] val dataDirProp =
    "dnpm.dip.data.dir"

  lazy val instance: PreparedQueryDB[Future,Monad[Future],RDQueryCriteria,String] =
    Option(System.getProperty(dataDirProp))
      .map( dir =>
        new FSBackedPreparedQueryDB[Future,Monad,RDQueryCriteria](
          new File(s"$dir/rd_data/prepared_queries")
        )
      )
      .getOrElse {
        log.warn(
          s"System property $dataDirProp for the data storage directory is undefined. Falling back to in-memory store for RD Prepared Queries!"
        )
        new InMemPreparedQueryDB[Future,Monad,RDQueryCriteria]
      }   

}
