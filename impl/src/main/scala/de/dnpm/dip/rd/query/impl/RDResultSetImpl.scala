package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.model.{
  Id,
  Patient,
  Snapshot
}
import de.dnpm.dip.service.query.{
  PatientFilter,
  PatientMatch,
  Query,
  BaseResultSet,
  ReportingOps,
  Distribution
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDQueryCriteria,
  RDResultSet,
  RDResultSummary
}


class RDResultSetImpl(
  val id: Query.Id,
  val results: Seq[(Snapshot[RDPatientRecord],RDQueryCriteria)]
)
extends RDResultSet
with BaseResultSet[RDPatientRecord,RDQueryCriteria]
{

  override def summary = {

    val patRecs =
      results.collect {
        case (Snapshot(patRec,_),_) => patRec
      }

    val patients =
      patRecs.map(_.patient)

    RDResultSummary(
      id,
      patRecs.size,
//      PatientFilter.on(patients),
      ReportingOps.FrequencyDistribution(
        patients.map(_.managingSite.get)  // .get safe here, because managingSite always set upon data import
      ),
      ReportingOps.FrequencyDistribution(
        patRecs.flatMap(
          _.diagnosis.categories.toList
        )
      ),
      ReportingOps.FrequencyDistribution(
        patRecs.flatMap(
          _.hpoTerms.map(_.value).toList
        )
      )
    )

  }


}
