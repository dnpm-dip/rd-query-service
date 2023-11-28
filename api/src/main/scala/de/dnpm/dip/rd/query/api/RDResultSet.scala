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
  totalDistributions: RDResultSummary.Distributions,
  distributionsByVariant: Seq[Entry[Coding[HGVS],RDResultSummary.Distributions]]
) 
extends ResultSet.Summary



object RDResultSummary
{

  final case class Distributions
  (
    diseaseCategories: Seq[ConceptCount[Coding[Orphanet]]],
    hpoTerms: Seq[ConceptCount[Coding[HPO]]]
  )


  implicit val writesDistributions: OWrites[Distributions] =
    Json.writes[Distributions]

  implicit val writes: OWrites[RDResultSummary] =
    Json.writes[RDResultSummary]
}


trait RDResultSet
extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type Summary = RDResultSummary
}

