package de.dnpm.dip.rd.query.api



import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.{
  Gender,
  Interval,
  Site,
}
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query,
  ResultSet,
  ConceptCount,
  Entry
}
import de.dnpm.dip.rd.model.{
  HGVS,
  HPO,
  Orphanet,
  RDPatientRecord
}
import play.api.libs.json.{
  Json,
  OWrites
}


final case class RDResultSummary
(
  id: Query.Id,
  numPatients: Int,
  genderDistribution: Seq[ConceptCount[Coding[Gender.Value]]],
  ageDistribution: Seq[ConceptCount[Interval[Int]]],
  siteDistribution: Seq[ConceptCount[Coding[Site]]],
  diagnosisCategoryDistribution: Seq[ConceptCount[Coding[Orphanet]]],
  hpoTermDistribution: Seq[ConceptCount[Coding[HPO]]],
  variantHpoTermDistributions: RDResultSummary.VariantAssociation[HPO],
  variantDiseaseCategoryDistributions: RDResultSummary.VariantAssociation[Orphanet]
) 
extends ResultSet.Summary


object RDResultSummary
{

  type VariantAssociation[T] =
    Seq[Entry[Coding[HGVS],Seq[ConceptCount[Coding[T]]]]]


  implicit val writes: OWrites[RDResultSummary] =
    Json.writes[RDResultSummary]
}


trait RDResultSet
extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type Summary = RDResultSummary
}

