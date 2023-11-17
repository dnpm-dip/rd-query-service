package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.service.query.ReportingOps
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
    VariantAssociationWith(
      records,
      _.hpoTerms.map(_.value).toList
    )


  def VariantDiseaseCategoryAssociation(
    records: Seq[RDPatientRecord]
  ): VariantAssociation[Orphanet] =
    VariantAssociationWith(
      records,
      _.diagnosis.categories.toList
    )


  private def VariantAssociationWith[T](
    records: Seq[RDPatientRecord],
    codingsOn: RDPatientRecord => List[Coding[T]]
  ): VariantAssociation[T] = {

      records.foldLeft(
        Map.empty[Coding[HGVS],List[Coding[T]]]
      ){
        (acc,record) =>
      
        val codings =
          codingsOn(record)
      
        val variants =
          record
            .ngsReports
            .toList
            .flatMap(
              _.variants.getOrElse(List.empty)
            )
            .flatMap(_.proteinChange)
            .distinct
      
        variants.foldLeft(acc){
          (accpr,aaChg) =>
            accpr.updatedWith(aaChg)(
              _.map(codings ::: _)
               .orElse(Some(codings))
             )
      
        }
      
      }
      .map {
        case (aaChg,codings) =>
          aaChg -> FrequencyDistribution(codings)
      }
      .toSeq

  }

}
object RDReportingOps extends RDReportingOps
