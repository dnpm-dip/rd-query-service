package de.dnpm.dip.rd.query.api


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  HPO,
}
import play.api.libs.json.{
  Json,
  Format,
  OFormat
}


object Operator extends Enumeration
{
  val AND, OR = Value

  implicit val format: Format[Value] =
    Json.formatEnum(this)
}

trait Criteria
{
  val operator: Option[Operator.Value]
}


final case class RDQueryCriteria
(
  diagnoses: Option[Set[Coding[RDDiagnosis.Category]]],
  hpoTerms: Option[Set[Coding[HPO]]],
  variants: Option[Set[VariantCriteria]]
)



final case class DiagnosisCriteria
(
  operator: Option[Operator.Value],
  categories: Set[Coding[RDDiagnosis.Category]],
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
  gene: Option[Coding[HGNC]],
  cDNAChange: Option[Coding[HGVS.DNA]],
  gDNAChange: Option[Coding[HGVS.DNA]],
  proteinChange: Option[Coding[HGVS.Protein]]
)

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

