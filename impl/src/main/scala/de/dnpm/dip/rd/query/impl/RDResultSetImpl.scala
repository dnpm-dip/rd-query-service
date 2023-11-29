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
  Distribution
}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDQueryCriteria,
  RDResultSet,
  RDResultSummary,
  RDDistributions
}


class RDResultSetImpl(
  val id: Query.Id,
  val results: Seq[(Snapshot[RDPatientRecord],RDQueryCriteria)]
)
extends RDResultSet
with BaseResultSet[RDPatientRecord,RDQueryCriteria]
{

  import RDReportingOps._

  override def summary(
    filter: RDPatientRecord => Boolean
  ) = {

    val records =
      results.collect {
        case (Snapshot(patRec,_),_) if (filter(patRec)) => patRec
      }

    val patients =
      records.map(_.patient)


    RDResultSummary(
      id,
      records.size,
      RDResultSummary.Distributions(
        DistributionOf(patients.map(_.gender)),
        AgeDistribution(patients.map(_.age)),
        DistributionOf(patients.flatMap(_.managingSite)),
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
      RDResultSummary.GroupedDistributions(
        DistributionsByVariant(records)
      )
    )

/*
    RDResultSummary(
      id,
      records.size,
      DistributionOf(patients.map(_.gender)),
      AgeDistribution(patients.map(_.age)),
      DistributionOf(patients.flatMap(_.managingSite)),
      RDResultSummary.Distributions(
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
*/
  }

}
