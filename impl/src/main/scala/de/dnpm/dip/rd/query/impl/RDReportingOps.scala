package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.service.query.{
  ReportingOps,
  Entry
}
import de.dnpm.dip.rd.model.{
  HGVS,
  HPO,
  Orphanet,
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api.RDResultSummary.VariantAssociation



trait RDReportingOps extends ReportingOps
{

  def VariantHPOAssociation(
    records: Seq[RDPatientRecord]
  ): VariantAssociation[HPO] =
    DistributionsOn(
      records
    )(
      _.ngsReports
       .toList
       .flatMap(
         _.variants.getOrElse(List.empty)
       )
       .flatMap(_.proteinChange)
       .distinct,
      _.hpoTerms.map(_.value).toList,
    )


  def VariantDiseaseCategoryAssociation(
    records: Seq[RDPatientRecord]
  ): VariantAssociation[Orphanet] =
    DistributionsOn(
      records
    )(
      _.ngsReports
       .toList
       .flatMap(
         _.variants.getOrElse(List.empty)
       )
       .flatMap(_.proteinChange)
       .distinct,
      _.diagnosis.categories.toList
    )

}
object RDReportingOps extends RDReportingOps
