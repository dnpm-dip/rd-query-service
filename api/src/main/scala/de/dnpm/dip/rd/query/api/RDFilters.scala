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
  Orphanet
}


final case class HPOFilter
(
  value: Option[Set[Coding[HPO]]]
)
extends (HPOTerm => Boolean)
{
  override def apply(term: HPOTerm) =
    value match {
      case Some(hpos) if hpos.nonEmpty =>
        hpos exists (_.code == term.value.code)
      case _ => true
    }
}


final case class DiagnosisFilter
(
  category: Option[Set[Coding[Orphanet]]]
)
extends (RDDiagnosis => Boolean)
{
  override def apply(diag: RDDiagnosis) =
    category match {
       case Some(orphas) if orphas.nonEmpty =>
         diag.categories exists (c => orphas exists (_.code == c.code))

       case _ => true
    }
}


final case class RDFilters
(
  patientFilter: PatientFilter,
  hpoFilter: HPOFilter,
  diagnosisFilter: DiagnosisFilter
)
extends Filters[RDPatientRecord]
{

  override def apply(patRec: RDPatientRecord): Boolean = {

    patientFilter(patRec.patient) && 
    patRec.hpoTerms.exists(term => hpoFilter(term)) &&
    diagnosisFilter(patRec.diagnosis)
  }

}


object RDFilters
{

  implicit val writesHPOFilter: OWrites[HPOFilter] =
    Json.writes[HPOFilter]

  implicit val writesDiagFilter: OWrites[DiagnosisFilter] =
    Json.writes[DiagnosisFilter]

  implicit val writes: OWrites[RDFilters] =
    Json.writes[RDFilters]

}
