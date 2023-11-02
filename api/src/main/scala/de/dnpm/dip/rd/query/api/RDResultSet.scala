package de.dnpm.dip.rd.query.api



import de.dnpm.dip.service.query.{
  Query,
  ResultSet
}
import de.dnpm.dip.rd.model.RDPatientRecord
import play.api.libs.json.{
  Json,
  OWrites
}

final case class RDResultSummary
(
  id: Query.Id,
  numPatients: Int
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

