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
  distributions: RDResultSummary.Distributions,
  groupedDistributions: RDResultSummary.GroupedDistributions
) 
extends ResultSet.Summary
{
  type DistributionsType = RDResultSummary.Distributions
}


case class RDDistributions
(
  diseaseCategory: Seq[ConceptCount[Coding[Orphanet]]],
  hpoTerm: Seq[ConceptCount[Coding[HPO]]]
)


object RDResultSummary
{

  final case class Distributions
  (
    gender: Seq[ConceptCount[Coding[Gender.Value]]],
    age: Seq[ConceptCount[Interval[Int]]],
    site: Seq[ConceptCount[Coding[Site]]],
    diseaseCategory: Seq[ConceptCount[Coding[Orphanet]]],
    hpoTerm: Seq[ConceptCount[Coding[HPO]]]
  )
  extends ResultSet.Distributions


  final case class GroupedDistributions
  (
    variant: Seq[Entry[Coding[HGVS],RDDistributions]]
  )


  implicit val writesDistributions: OWrites[Distributions] =
    Json.writes[Distributions]

  implicit val writesRDDistributions: OWrites[RDDistributions] =
    Json.writes[RDDistributions]

  implicit val writesGroupedDistributions: OWrites[GroupedDistributions] =
    Json.writes[GroupedDistributions]

  implicit val writes: OWrites[RDResultSummary] =
    Json.writes[RDResultSummary]
}


trait RDResultSet
extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type Summary = RDResultSummary
}

