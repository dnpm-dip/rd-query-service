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
  RDPatientRecord
}
import de.dnpm.dip.rd.query.api.RDResultSet.Diagnostics.Distributions



trait RDReportingOps
{

  def distributionsByVariant(
    records: Seq[RDPatientRecord]
  ): Seq[Entry[String,Distributions]] = {

    records.foldLeft(
      Map.empty[String,(Seq[Coding[HPO]],Seq[Coding[RDDiagnosis.Systems]])]
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

      val diagnosisCodes =
        record.diagnoses
         .flatMap(_.codes)
         .toList


      variants.foldLeft(acc){
        case (accPr,variant) =>
          accPr.updatedWith(variant)(
            _.map {
               case (hpos,codes) => (hpos :++ hpoTerms, codes :++ diagnosisCodes)
            }
            .orElse(
              Some((hpoTerms,diagnosisCodes))
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
