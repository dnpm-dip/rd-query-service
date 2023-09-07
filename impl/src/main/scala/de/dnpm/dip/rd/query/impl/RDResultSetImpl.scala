package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.model.{
  Id,
  Patient,
  Snapshot
}
import de.dnpm.dip.service.query.{
  PatientMatch,
  Query,
  BaseResultSet
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDCriteria,
  RDResultSet,
  RDResultSummary
}


class RDResultSetImpl(
  val id: Query.Id,
  val results: Seq[(Snapshot[RDPatientRecord],RDCriteria)]
)
extends RDResultSet
with BaseResultSet[RDPatientRecord,RDCriteria]
{

  override lazy val summary =
    RDResultSummary(
      id,
      results.size
    )

}
