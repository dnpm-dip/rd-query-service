package de.dnpm.dip.rd.query.api


import play.api.libs.json.{
  Json,
  OWrites
}
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Age,
  Interval,
  Gender,
  Patient,
  VitalStatus,
  Site
}
import de.dnpm.dip.service.query.{
  Filters,
  PatientFilter
}
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  RDPatientRecord,
  HPO,
  HPOTerm,
}


final case class HPOFilter
(
  value: Option[Set[Coding[HPO]]]
)


final case class DiagnosisFilter
(
  category: Option[Set[Coding[RDDiagnosis.Category]]]
)


final case class RDFilters
(
  patientFilter: PatientFilter,
  hpoFilter: HPOFilter,
  diagnosisFilter: DiagnosisFilter
)
extends Filters[RDPatientRecord]

object RDFilters
{

  lazy val empty: RDFilters =
    RDFilters(
      PatientFilter.empty,
      HPOFilter(None),
      DiagnosisFilter(None)
    )


  implicit val writesHPOFilter: OWrites[HPOFilter] =
    Json.writes[HPOFilter]

  implicit val writesDiagFilter: OWrites[DiagnosisFilter] =
    Json.writes[DiagnosisFilter]

  implicit val writes: OWrites[RDFilters] =
    Json.writes[RDFilters]

}
