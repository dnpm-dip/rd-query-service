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
  val results: Seq[Query.Match[RDPatientRecord,RDQueryCriteria]]
)
extends RDResultSet
{

  import RDReportingOps._
  import RDResultSet.Diagnostics


  override def diagnostics(
    filter: RDPatientRecord => Boolean
  ): Diagnostics = {

    val records =
      patientRecords(filter)

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

  }

}
