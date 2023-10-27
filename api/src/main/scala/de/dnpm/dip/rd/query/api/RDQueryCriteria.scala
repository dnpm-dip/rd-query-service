package de.dnpm.dip.rd.query.api


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  HPO,
  HGVS,
  Orphanet,
  Variant,
  RDPatientRecord
}
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query,
}
import play.api.libs.json.{
  Json,
  Format
}


final case class RDQueryCriteria
(
//  diagnoses: Option[Set[DiagnosisCriteria]],
  diagnoses: Option[Set[Coding[Orphanet]]],
  hpoTerms: Option[Set[Coding[HPO]]],
  variants: Option[Set[VariantCriteria]]
)

/*
final case class DiagnosisCriteria
(
  categories: Set[Coding[Orphanet]],
//  status: Option[Coding[RDDiagnosis.Status.Value]]
)
*/

final case class VariantCriteria
(
  gene: Option[Coding[HGNC]],
  cDNAChange: Option[Coding[HGVS]],
  gDNAChange: Option[Coding[HGVS]],
  proteinChange: Option[Coding[HGVS]],
  acmgClass: Option[Set[Coding[Variant.ACMGClass]]],
  acmgCriteria: Option[Set[Coding[Variant.ACMGCriteria]]],
  zygosity: Option[Set[Coding[Variant.Zygosity]]],
  segregationAnalysis: Option[Set[Coding[Variant.SegregationAnalysis]]],
  modeOfInheritance: Option[Set[Coding[Variant.InheritanceMode]]],
  significance: Option[Set[Coding[Variant.Significance]]],
)

object RDQueryCriteria
{
//  implicit val formatDiagCrit: Format[DiagnosisCriteria] =
//    Json.format[DiagnosisCriteria]

  implicit val formatVarCrit: Format[VariantCriteria] =
    Json.format[VariantCriteria]

  implicit val format: Format[RDQueryCriteria] =
    Json.format[RDQueryCriteria]
}



final case class RDFilters
(
  patientFilter: PatientFilter
)
extends Query.Filters

object RDFilters
{
  implicit val format: Format[RDFilters] =
    Json.format[RDFilters]
}
