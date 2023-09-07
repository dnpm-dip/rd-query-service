package de.dnpm.dip.rd.query.api



import de.dnpm.dip.service.query.{
  Query,
  ResultSet
}
import de.dnpm.dip.rd.model.RDPatientRecord


final case class RDResultSummary
(
  id: Query.Id,
  numPatients: Int
) 
extends ResultSet.Summary


trait RDResultSet
extends ResultSet[RDPatientRecord,RDCriteria]
{
  type Summary = RDResultSummary
}

