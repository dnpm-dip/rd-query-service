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
  Format,
  OFormat
}


object Operator extends Enumeration
{
  val AND, OR = Value

/*
  def unapply(s: String): Option[Operator.Value] =
    s.toLowerCase match {
      case "and" | "&&" => Some(AND)
      case "or"  | "||" => Some(OR)
      case _            => None
    }
*/

  implicit val format: Format[Value] =
    Json.formatEnum(this)
}

trait Criteria
{
  val operator: Option[Operator.Value]
}


final case class RDQueryCriteria
(
  diagnoses: Option[Set[Coding[Orphanet]]],
  hpoTerms: Option[Set[Coding[HPO]]],
  variants: Option[Set[VariantCriteria]]
)

/*
final case class RDQueryCriteria
(
  operator: Option[Operator.Value],
  diagnosisCriteria: Option[DiagnosisCriteria],
  hpoCriteria: Option[HPOCriteria],
  variantCriteria: Option[Set[VariantCriteria]]
)
extends Criteria
*/


final case class DiagnosisCriteria
(
  operator: Option[Operator.Value],
  categories: Set[Coding[Orphanet]],
)
extends Criteria

final case class HPOCriteria
(
  operator: Option[Operator.Value],
  hpoCodings: Set[Coding[HPO]],
)
extends Criteria


final case class VariantCriteria
(
//  operator: Option[Operator.Value],
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
//extends Criteria

object RDQueryCriteria
{
  implicit val formatDiagCrit: Format[DiagnosisCriteria] =
    Json.format[DiagnosisCriteria]

  implicit val formatHpoCrit: Format[HPOCriteria] =
    Json.format[HPOCriteria]

  implicit val formatVarCrit: OFormat[VariantCriteria] =
    Json.format[VariantCriteria]

  implicit val format: OFormat[RDQueryCriteria] =
    Json.format[RDQueryCriteria]
}

