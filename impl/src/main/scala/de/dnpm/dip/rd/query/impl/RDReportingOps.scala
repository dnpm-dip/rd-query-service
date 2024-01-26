package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.coding.Coding
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.service.query.{
  Distribution,
  Entry
}
import de.dnpm.dip.rd.model.{
  HPO,
  Orphanet,
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api.RDResultSet.Diagnostics.Distributions



trait RDReportingOps
{

  def distributionsByVariant(
    records: Seq[RDPatientRecord]
  ): Seq[Entry[Coding[HGVS],Distributions]] = {

    records.foldLeft(
      Map.empty[Coding[HGVS],(Seq[Coding[HPO]],Seq[Coding[Orphanet]])]
    ){
      (acc,record) =>

      val variants =
        record
          .ngsReports
          .toList
          .flatMap(
            _.variants.getOrElse(List.empty)
          )
          .flatMap(_.proteinChange)
          .distinct

      val hpoTerms =   
        record
          .hpoTerms
          .map(_.value)
          .toList

      val diseaseCategories =
        record.diagnosis
          .categories
          .toList


      variants.foldLeft(acc){
        case (accPr,variant) =>
          accPr.updatedWith(variant)(
            _.map {
               case (hpos,orphas) => (hpos :++ hpoTerms, orphas :++ diseaseCategories)
            }
            .orElse(
              Some((hpoTerms,diseaseCategories))
            )
          )
      }

    }
    .map {
      case (variant,(hpos,orphas)) =>
        Entry(
          variant,
          Distributions(
            Distribution.of(orphas),
            Distribution.of(hpos)
          )
        )
    }
    .toSeq


  }

}
object RDReportingOps extends RDReportingOps
