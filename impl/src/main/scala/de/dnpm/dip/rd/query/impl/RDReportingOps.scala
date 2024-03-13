package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.util.DisplayLabel
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.service.query.{
  Distribution,
  Entry
}
import de.dnpm.dip.rd.model.{
  HPO,
  RDDiagnosis,
  RDPatientRecord,
  Variant
}
import de.dnpm.dip.rd.query.api.RDResultSet.Diagnostics.Distributions



trait RDReportingOps
{

  def distributionsByVariant(
    records: Seq[RDPatientRecord]
  ): Seq[Entry[String,Distributions]] = {

    records.foldLeft(
      Map.empty[String,(Seq[Coding[HPO]],Seq[Coding[RDDiagnosis.Category]])]
    ){
      (acc,record) =>

      val variants =
        record
          .getNgsReports
          .flatMap(_.variants)
          .map(DisplayLabel.of(_).value)
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
