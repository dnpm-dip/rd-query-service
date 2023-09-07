package de.dnpm.dip.rd.query.api


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.rd.model.{
  RDDiagnosis,
  HPO,
  HGVS,
  Variant,
  RDPatientRecord
}
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query,
  UseCaseConfig
}



final case class RDCriteria
(
  diagnoses: Option[Set[DiagnosisCriteria]],
  hpoTerms: Option[Set[Coding[HPO]]],
  variants: Option[Set[VariantCriteria]]
)


final case class DiagnosisCriteria
(
  category: Option[Coding[RDDiagnosis.Category]],
  status: Option[Coding[RDDiagnosis.Status.Value]]
)

final case class VariantCriteria
(
  gene: Option[Coding[HGNC]],
  cDNAChange: Option[Coding[HGVS]],
  gDNAChange: Option[Coding[HGVS]],
  proteinChange: Option[Coding[HGVS]],
)


final case class RDFilters
(
  patientFilter: PatientFilter
)
extends Query.Filters



sealed trait RDConfig extends UseCaseConfig
{

  type PatientRecord = RDPatientRecord

  type Criteria = RDCriteria

  type Filters = RDFilters

  type Results = RDResultSet
}


