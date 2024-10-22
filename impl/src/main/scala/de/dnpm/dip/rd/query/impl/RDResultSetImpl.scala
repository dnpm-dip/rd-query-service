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


  import scala.language.implicitConversions

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
        f.category match {
           case Some(orphas) if orphas.nonEmpty => diag.categories exists (c => orphas exists (_.code == c.code))
           case _ => true
        }

    record =>
      filter.patient(record.patient) &&
      record.hpoTerms.exists(filter.hpo) &&
      filter.diagnosis(record.diagnosis)

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
          records.flatMap(_.diagnosis.categories.toList)
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
