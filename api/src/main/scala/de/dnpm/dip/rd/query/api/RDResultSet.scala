package de.dnpm.dip.rd.query.api



import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.model.{
  Gender,
  Interval,
  Site,
}
import de.dnpm.dip.service.query.{
  PatientFilter,
  Query,
  ResultSet,
//  ConceptCount,
  Entry,
  Distribution
}
import ResultSet.Demographics
import de.dnpm.dip.rd.model.{
  HPO,
  Orphanet,
  RDPatientRecord
}
import play.api.libs.json.{
  Json,
  OWrites
}


trait RDResultSet extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type SummaryType = RDResultSet.Summary
}

object RDResultSet
{

  object Diagnostics
  {

    final case class Distributions
    (
      diseaseCategoryDistribution: Distribution[Coding[Orphanet]],
      hpoTermDistribution: Distribution[Coding[HPO]]
    )

    implicit val writesDistributions: OWrites[Distributions] =
      Json.writes[Distributions]

  }

  final case class Diagnostics
  (
    overall: Diagnostics.Distributions,
    variants: Seq[Entry[Coding[HGVS],Diagnostics.Distributions]]
  )


  final case class Summary
  (
    id: Query.Id,
    patientCount: Int,
    demographics: Demographics,
    diagnostics: Diagnostics
  )
  extends ResultSet.Summary


  implicit val writesDiagnostics: OWrites[Diagnostics] =
    Json.writes[Diagnostics]

  implicit val writesSummary: OWrites[Summary] =
    Json.writes[Summary]

}

