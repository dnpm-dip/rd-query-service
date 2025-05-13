package de.dnpm.dip.rd.query.api



import de.dnpm.dip.coding.Coding
import de.dnpm.dip.service.{
  Entry,
  Distribution
}
import de.dnpm.dip.service.query.ResultSet
import de.dnpm.dip.rd.model.{
  HPO,
  RDDiagnosis,
  RDPatientRecord
}
import play.api.libs.json.{
  Json,
  OWrites
}


trait RDResultSet extends ResultSet[RDPatientRecord,RDQueryCriteria]
{
  type Filter = RDFilters

  def diagnostics(
    filter: RDFilters
  ): RDResultSet.Diagnostics

}

object RDResultSet
{

  object Diagnostics
  {

    final case class Distributions
    (
      diagnoses: Distribution[Coding[RDDiagnosis.Systems]],
      hpoTerms: Distribution[Coding[HPO]]
    )

    implicit val writesDistributions: OWrites[Distributions] =
      Json.writes[Distributions]

    implicit val writesDiagnostics: OWrites[Diagnostics] =
      Json.writes[Diagnostics]

  }

  final case class Diagnostics
  (
    overallDistributions: Diagnostics.Distributions,
    distributionsByVariant: Seq[Entry[String,Diagnostics.Distributions]]
  )

}

