package de.dnpm.dip.rd.query.impl


import cats.data.Ior
import cats.data.Ior.{Left,Right,Both}
import de.dnpm.dip.coding.Coding
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDQueryCriteria,
  DiagnosisCriteria,
  VariantCriteria
}


private trait RDQueryCriteriaOps
{

  private[impl] implicit class RDQueryCriteriaExtensions(
    criteria: RDQueryCriteria
  ){

    def isEmpty: Boolean =
      (
        criteria.diagnoses.getOrElse(Set.empty) ++
        criteria.hpoTerms.getOrElse(Set.empty) ++
        criteria.variants.getOrElse(Set.empty)
      )
      .isEmpty

    def nonEmpty = !criteria.isEmpty


    def intersect(other: RDQueryCriteria): RDQueryCriteria =
      RDQueryCriteria(
        criteria.diagnoses.map(_ intersect other.diagnoses.getOrElse(Set.empty)),
        criteria.hpoTerms.map(_ intersect other.hpoTerms.getOrElse(Set.empty)),
        criteria.variants.map(_ intersect other.variants.getOrElse(Set.empty)),
      )

    def &(other: RDQueryCriteria) = criteria intersect other

  }



  private def checkMatches(
    bs: Boolean*
  )(
    strict: Boolean
  ): Boolean =
    if (strict)
      bs forall (_ == true)
    else
      bs exists (_ == true)




  def criteriaMatcher(
    strict: Boolean = true
  ): RDQueryCriteria => (RDPatientRecord => Option[RDQueryCriteria]) = {

    rdCriteria => 

      rdCriteria match {

        // If criteria object is empty, i.e. no query criteria are defined at all, any patient record matches
        case RDQueryCriteria(None,None,None) => 
          patientRecord => Some(rdCriteria)

          
        case RDQueryCriteria(diagnosisCriteria,hpoCriteria,variantCriteria) => 

          patientRecord => 

            val (diagnosisMatches,diagnosisOk) =
              diagnosisCriteria match {
                case Some(crit) if crit.nonEmpty => 
                  val matches =
                    crit.filter {
                      case DiagnosisCriteria(category,status) =>
                        category.map(coding => patientRecord.diagnosis.categories.exists(_.code == coding.code)).getOrElse(true) &&
                        status.map(_.code == patientRecord.diagnosis.status.code).getOrElse(true)
                    }
                  Some(matches).filter(_.nonEmpty) -> (crit intersect matches).nonEmpty
                
                case _ => None -> true
              }

            val (hpoMatches,hpoOk) =
              hpoCriteria match {

                case Some(crit) if crit.nonEmpty => 

                  // HPO Codings occurring in the patient record
                  val hpoCodings =
                    patientRecord
                      .hpoTerms
                      .toList
//                      .getOrElse(List.empty)
                      .map(_.value)
                      .distinctBy(_.code)
                      .toSet

                
                  val matches =
                    crit intersect hpoCodings

                  Some(matches).filter(_.nonEmpty) -> (crit intersect matches).nonEmpty
                
                case _ => None -> true
              }
           
            val (variantMatches,variantsOk) =
              variantCriteria match {

                case Some(crit) if crit.nonEmpty => 

                  val variants =
                    patientRecord
                      .ngsReports.toList
                      .flatMap(_.variants.getOrElse(List.empty))
           
                  val matches =
                    crit.filter {
                      case VariantCriteria(gene,cDNAChange,gDNAChange,proteinChange) =>
           
                        variants.exists {
                          variant =>
           
                          gene.map(_.code == variant.gene.code).getOrElse(true) &&
                          // DNA and Protein change checks not based on code equality,
                          // but whether the query criterion is a substring of the occurring code 
                          cDNAChange.map(c => variant.cDNAChange.exists(_.code.value contains c.code.value)).getOrElse(true) &&
                          gDNAChange.map(c => variant.gDNAChange.exists(_.code.value contains c.code.value)).getOrElse(true) &&
                          proteinChange.map(c => variant.proteinChange.exists(_.code.value contains c.code.value)).getOrElse(true)
                        }
                    }

                  Some(matches).filter(_.nonEmpty) -> (crit intersect matches).nonEmpty
                
                case _ => None -> true
            }

            if (checkMatches(diagnosisOk,hpoOk,variantsOk)(strict))
              Some(
                RDQueryCriteria(
                  diagnosisMatches,
                  hpoMatches,
                  variantMatches
                )
              )
            else 
              None

        }
      }

}

private object RDQueryCriteriaOps extends RDQueryCriteriaOps

