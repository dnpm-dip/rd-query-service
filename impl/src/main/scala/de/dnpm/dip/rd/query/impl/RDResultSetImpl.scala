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
    Diagnostics,
    Distributions
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
        Distributions(
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

/*
  override def diagnostics(
    f: RDPatientRecord => Boolean
  ): RDResultSet.Diagnostics = {

    val patients =
      records.collect {
        case record if f(record) => record.patient
      }

    RDResultSet.Diagnostics(
      id,
      RDResultSet.Distributions(
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

  }
*/
/*  
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

  }
*/
}
