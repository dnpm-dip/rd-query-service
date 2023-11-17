package de.dnpm.dip.rd.query.api



import de.dnpm.dip.coding.Coding
import de.dnpm.dip.model.Site
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query,
  ResultSet,
  ConceptCount
}
import de.dnpm.dip.rd.model.{
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
//  patientFilter: PatientFilter,
  siteDistribution: Seq[ConceptCount[Coding[Site]]],
  diagnosisCategoryDistribution: Seq[ConceptCount[Coding[Orphanet]]],
  hpoTermDistribution: Seq[ConceptCount[Coding[HPO]]]
) 
extends ResultSet.Summary

object RDResultSummary
{
  implicit val writes: OWrites[RDResultSummary] =
    Json.writes[RDResultSummary]
}


trait RDResultSet
extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type Summary = RDResultSummary
}

