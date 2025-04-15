package de.dnpm.dip.rd.query.api


import play.api.libs.json.{
  Json,
  OWrites
}
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.service.query.{
  Filters,
  PatientFilter
}
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  RDPatientRecord,
  HPO,
}


final case class HPOFilter
(
  value: Option[Set[Coding[HPO]]]
)


final case class DiagnosisFilter
(
  codes: Option[Set[Coding[RDDiagnosis.Systems]]]
)


final case class RDFilters
(
  patient: PatientFilter,
  hpo: HPOFilter,
  diagnosis: DiagnosisFilter
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
