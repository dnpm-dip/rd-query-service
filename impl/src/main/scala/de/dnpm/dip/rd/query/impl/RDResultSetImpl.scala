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
  BaseResultSet,
  Distribution
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDQueryCriteria,
  RDResultSet,
}


class RDResultSetImpl(
  val id: Query.Id,
  val results: Seq[(Snapshot[RDPatientRecord],RDQueryCriteria)]
)
extends RDResultSet
with BaseResultSet[RDPatientRecord,RDQueryCriteria]
{

  import RDReportingOps._
  import RDResultSet.{
    Summary,
    Diagnostics
  }

  override def summary(
    f: RDPatientRecord => Boolean
  ): RDResultSet.Summary = {

    val patients =
      records.collect {
        case record if f(record) => record.patient
      }

    Summary(
      id,
      patients.size,
      ResultSet.Demographics.on(patients),
      Diagnostics(
        Diagnostics.Distributions(
          DistributionOf(
            records.flatMap(
              _.diagnosis.categories.toList
            )
          ),
          DistributionOf(
            records.flatMap(
              _.hpoTerms.map(_.value).toList
            )
          )
        ),
        DistributionsByVariant(records)
      )
    )

  }

}
