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
  ResultSet,
  Distribution
}
import de.dnpm.dip.rd.model.{
  HPOTerm,
  RDDiagnosis,
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api.{
  RDQueryCriteria,
  RDResultSet,
  RDFilters,
  HPOFilter,
  DiagnosisFilter
}


class RDResultSetImpl(
  val id: Query.Id,
  val criteria: RDQueryCriteria,
  val results: Seq[(Snapshot[RDPatientRecord],Option[RDQueryCriteria])]
)
extends RDResultSet
{

  import RDReportingOps._
  import RDResultSet.{
    Summary,
    Diagnostics
  }

  private lazy val records =
    results.collect { case (Snapshot(record,_),_) => record }


  override def summary(
    filter: RDPatientRecord => Boolean
  ): RDResultSet.Summary = {

    val patients =
      records.collect {
        case record if filter(record) => record.patient
      }

    Summary(
      id,
      patients.size,
      ResultSet.Demographics.on(patients),
      Diagnostics(
        Diagnostics.Distributions(
          Distribution.of(
            records.flatMap(
              _.diagnosis.categories.toList
            )
          ),
          Distribution.of(
            records.flatMap(
              _.hpoTerms.map(_.value).toList
            )
          )
        ),
        distributionsByVariant(records)
      )
    )

  }

}
