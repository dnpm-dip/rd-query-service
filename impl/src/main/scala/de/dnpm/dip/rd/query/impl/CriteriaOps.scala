package de.dnpm.dip.rd.query.impl


import cats.data.Ior
import cats.data.Ior.{Left,Right,Both}
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.{
  RDCriteria,
  DiagnosisCriteria,
  VariantCriteria
}


trait CriteriaOps
{

  private def isEmpty(criteria: RDCriteria): Boolean =
    (
      criteria.diagnoses.map(_.size).getOrElse(0) +
      criteria.hpoTerms.map(_.size).getOrElse(0) +
      criteria.variants.map(_.size).getOrElse(0)
    ) > 0

//    criteria.diagnoses.filter(_.nonEmpty).isEmpty ||
//    criteria.hpoTerms.filter(_.nonEmpty).isEmpty ||
//    criteria.variants.filter(_.nonEmpty).isEmpty
  

  val criteriaMatcher: RDCriteria => (RDPatientRecord => Option[RDCriteria]) = {

    criteria => 

      criteria match {

        // If criteria object is empty, i.e. no query criteria are set, any patient record matches
        case RDCriteria(None,None,None) => 
          patientRecord => Some(criteria)

          
        case RDCriteria(diagnosisCriteria,hpoTerms,variantCriteria) => 

          patientRecord => 

            val diagnosisMatches =
              diagnosisCriteria
                .filter(_.nonEmpty)
                .map(
                  _.filter {
                    case DiagnosisCriteria(category,status) =>
                      category.map(_.code == patientRecord.diagnosis.category.code).getOrElse(true) &&
                      status.map(_.code == patientRecord.diagnosis.status.code).getOrElse(true)
                  }
                )

            val hpoMatches =
              hpoTerms
                .filter(_.nonEmpty)
                .map { hpos =>
                   
                  val hpoCodings =
                    patientRecord.hpoTerms
                      .getOrElse(List.empty)
                      .map(_.value)
                
                  hpos.filter(
                    hpo => hpoCodings.exists(_.code == hpo.code)
                  )
                }
           
            val variantMatches =
              variantCriteria
                .filter(_.nonEmpty)
                .map { variantCriteria =>
           
                  val variants =
                    patientRecord.ngsReport
                      .variants
                      .getOrElse(List.empty)
           
                  variantCriteria.filter {
                    case VariantCriteria(gene,cDNAChange,gDNAChange,proteinChange) =>
           
                      variants.exists {
                        variant =>
           
                          gene.map(_.code == variant.gene.code).getOrElse(true) &&
                          cDNAChange.map(c => variant.cDNAChange.exists(_.code == c.code)).getOrElse(true) &&
                          gDNAChange.map(c => variant.gDNAChange.exists(_.code == c.code)).getOrElse(true) &&
                          proteinChange.map(c => variant.proteinChange.exists(_.code == c.code)).getOrElse(true)
           
                      }
                  }
           
                }
          
              val (diagOk,hpoOk,variantsOk) = (
                (diagnosisCriteria,diagnosisMatches) match {
                  case (None | Some(Nil),_) | (Some(_),Some(_)) => true
                  case _                                        => false
                },
                (hpoTerms,hpoMatches) match {
                  case (None | Some(Nil),_) | (Some(_),Some(_)) => true
                  case _                                        => false
                },
                (variantCriteria,variantMatches) match {
                  case (None | Some(Nil),_) | (Some(_),Some(_)) => true
                  case _                                        => false
                }
              )

              if (diagOk && hpoOk && variantsOk)
                Some(
                  RDCriteria(
                    diagnosisMatches,
                    hpoMatches,
                    variantMatches
                  )
                )
              else 
                None

        }
      }

/*      
  val criteriaMatcher: RDCriteria => (RDPatientRecord => RDCriteria) =
    criteria => 

      patientRecord => {

        val diagnosisMatches =
          criteria
            .diagnoses
            .filter(_.nonEmpty)
            .map(
              _.filter {
                case DiagnosisCriteria(category,status) =>
                  category.map(_.code == patientRecord.diagnosis.category.code).getOrElse(true) &&
                  status.map(_.code == patientRecord.diagnosis.status.code).getOrElse(true)
              }
            )

        val hpoMatches =
          criteria
            .hpoTerms
            .filter(_.nonEmpty)
            .map { hpos =>
               
              val hpoCodings =
                patientRecord.hpoTerms
                  .getOrElse(List.empty)
                  .map(_.value)
            
              hpos.filter(
                hpo => hpoCodings.exists(_.code == hpo.code)
              )
            }

        val variantMatches =
          criteria
            .variants
            .filter(_.nonEmpty)
            .map { variantCriteria =>

              val variants =
                patientRecord.ngsReport
                  .variants
                  .getOrElse(List.empty)

              variantCriteria.filter {
                case VariantCriteria(gene,cDNAChange,gDNAChange,proteinChange) =>

                  variants.exists {
                    variant =>

                      gene.map(_.code == variant.gene.code).getOrElse(true) &&
                      cDNAChange.map(c => variant.cDNAChange.exists(_.code == c.code)).getOrElse(true) &&
                      gDNAChange.map(c => variant.gDNAChange.exists(_.code == c.code)).getOrElse(true) &&
                      proteinChange.map(c => variant.proteinChange.exists(_.code == c.code)).getOrElse(true)

                  }
              }

            }


        RDCriteria(
          diagnosisMatches,
          hpoMatches,
          variantMatches
        )
      }
*/


/*     
  implicit class Extensions(val criteria: RDCriteria){

    def isEmpty: Boolean =
      CriteriaOps.isEmpty(criteria)

    def size: Int =
      criteria.diagnoses.map(_.size).getOrElse(0) +
      criteria.hpoTerms.map(_.size).getOrElse(0) +
      criteria.variants.map(_.size).getOrElse(0)

    def scalarProjection(other: RDCriteria): RDCriteria =
      RDCriteria(
        criteria.diagnoses.flatMap(c => other.diagnoses.map(c.intersect(_))),
        criteria.hpoTerms.flatMap(c => other.hpoTerms.map(c.intersect(_))),
        criteria.variants.flatMap(c => other.variants.map(c.intersect(_)))
      )

    def dot(other: RDCriteria): Double =
      criteria.scalarProjection(other).size.toDouble/criteria.size
  }
*/

}

object CriteriaOps extends CriteriaOps

