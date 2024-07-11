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
  val results: Seq[(Snapshot[RDPatientRecord],RDQueryCriteria)]
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

/*
  import scala.language.implicitConversions

  override implicit def toPredicate(
    filter: RDFilters
  ): RDPatientRecord => Boolean = {

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
      filter.patientFilter(record.patient) &&
      record.hpoTerms.exists(filter.hpoFilter) &&
      filter.diagnosisFilter(record.diagnosis)

  }
*/


  override def summary(
    filter: RDPatientRecord => Boolean
//    filter: RDFilters
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
