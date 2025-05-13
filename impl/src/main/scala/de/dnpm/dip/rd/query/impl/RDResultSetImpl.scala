package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.service.Distribution
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query
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


  override implicit def toPredicate[F >: RDFilters](
    f: F
  ): RDPatientRecord => Boolean = {

    import PatientFilter.Extensions._


    val filter = f.asInstanceOf[RDFilters]

    implicit def hpoFilterPredicate(f: HPOFilter): HPOTerm => Boolean =
      term =>
        f.value match {
          case Some(hpos) if hpos.nonEmpty => hpos exists (_.code == term.value.code)
          case _ => true
        }

    implicit def diagnosisFilterPredicate(f: DiagnosisFilter): RDDiagnosis => Boolean =
      diag =>
        f.codes match {
           case Some(cs) if cs.nonEmpty => diag.codes exists (c => cs exists (_.code == c.code))
           case _ => true
        }

    record =>
      filter.patient(record.patient) &&
      record.hpoTerms.exists(filter.hpo) &&
      record.diagnoses.exists(filter.diagnosis)
//      filter.diagnosis(record.diagnoses)

  }


  override lazy val defaultFilter: RDFilters = {

    val records =
      patientRecords(_ => true)

    RDFilters(
      PatientFilter.on(records),
      HPOFilter(
        Option(
          records.flatMap(_.hpoTerms.map(_.value).toList)
            .toSet
        )
      ),
      DiagnosisFilter(
        Option(
          records.flatMap(_.diagnoses.flatMap(_.codes).toList)
            .toSet
        )
      )
    )
  }


  override def diagnostics(
    filter: RDFilters
  ): Diagnostics = {

    val records =
      patientRecords(filter)

    Diagnostics(
      Diagnostics.Distributions(
        Distribution.of(
          records.flatMap(
            _.diagnoses.flatMap(_.codes).toList
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
