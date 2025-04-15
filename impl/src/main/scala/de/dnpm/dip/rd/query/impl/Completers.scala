package de.dnpm.dip.rd.query.impl


import cats.{
  Applicative,
  Id
}
import de.dnpm.dip.util.Completer
import de.dnpm.dip.model.BaseCompleters
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider,
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.icd.ICD10GM
import de.dnpm.dip.rd.model.{
  AlphaIDSE,
  HPO,
  Orphanet,
  RDDiagnosis,
}
import de.dnpm.dip.rd.query.api.{
  VariantCriteria,
  RDQueryCriteria
}


trait Completers extends BaseCompleters
{

  import Completer.syntax._


  implicit val hpOntology: CodeSystem[HPO]

  implicit val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]]

  implicit val ordo: CodeSystemProvider[Orphanet,Id,Applicative[Id]]

  implicit val alphaIdSE: CodeSystemProvider[AlphaIDSE,Id,Applicative[Id]]

  implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]


  implicit val variantCriteriaCompleter: Completer[VariantCriteria] = {

    val proteinChangeCompleter: Completer[Code[HGVS.Protein]] =
      code => code.copy(
        value = HGVS.Protein.to3LetterCode(code.value)
      )

    vc => vc.copy(
      gene          = vc.gene.complete,
      proteinChange = vc.proteinChange.map(proteinChangeCompleter)
    )
    
  }


  implicit val criteriaCompleter: Completer[RDQueryCriteria] = 
    criteria => criteria.copy(
      diagnoses = criteria.diagnoses.complete,
      hpoTerms  = criteria.hpoTerms.complete,
      variants  = criteria.variants.complete
    )
    



/*
  @deprecated("-","")
  private def expand[T,U >: T](
    coding: Coding[T],
    cs: CodeSystem[U]
  ): Set[Coding[T]] =
    (cs.concept(coding.code).toSet ++ cs.descendantsOf(coding.code))
      .map(
        _.toCoding(coding.system)
         .asInstanceOf[Coding[T]]
      )

  @deprecated("-","")
  private def expand[T,U >: T](
    coding: Coding[T],
    csp: CodeSystemProvider[U,Id,Applicative[Id]]
  ): Set[Coding[T]] =
    expand(
      coding,
      coding.version
        .flatMap(csp.get)
        .getOrElse(csp.latest)
    )
*/

  // Inverted index of Orphanet-Coding equivalent to a given ICD-10-Code,
  // used to speed-up the code expansion below, as ORDO contains the inverse
  // relationships of ICD-10 codes equivalent to a given ORDO concept
  object OrdoByIcd10Index
  {
    import Orphanet.extensions._ 
 
    private val index =
      scala.collection.mutable.Map.empty[Code[ICD10GM],Set[Coding[Orphanet]]]

    def get(code: Code[ICD10GM]): Set[Coding[Orphanet]] =
      index.getOrElseUpdate(
        code,
        ordo.latest
          .concepts
          .collect {
            case cpt if cpt.icd10Codes contains code => cpt.toCoding
          }
          .toSet
      )
  
  }
  
/*
  // Inverted index of Orphanet-Coding equivalent to a given ICD-10-Code,
  // used to speed-up the code expansion below, as ORDO contains the inverse
  // relationships of ICD-10 codes equivalent to a given ORDO concept
  private lazy val ordoByIcd10Index: Map[Code[ICD10GM],Set[Coding[Orphanet]]] = {

    import Orphanet.extensions._ 

    val icd10cs = icd10gm.latest

    ordo.latest
      .concepts
      .foldLeft(Map.empty[Code[ICD10GM],Set[Coding[Orphanet]]]){
        (acc,ordoConcept) =>

          val icd10s =
            ordoConcept.icd10Codes.flatMap(icd10cs.concept)

          icd10s.foldLeft(acc)(
            (accPr,icd10) =>
              accPr.updatedWith(icd10.code)(
                _.map(_ + ordoConcept)
                 .orElse(Some(Set(ordoConcept)))
              )
          )
              
      }

  }
*/

  // Custom "expansion" of Coding[RDDiagnosis.Systems] required because of the
  // synonymous relationships among Orphanet and ICD-10:
  // An Orphanet concept can contain (multiple) references to equivalent ICD-10 concepts.
  // Thus, for a given Orphanet or ICD-10 coding selected as query criterion,
  // the corresponding ICD-10 or Orphanet concepts should be included automatically as query criteria
  @deprecated("-","")
  private def expandEquivalentCodings(
    coding: Coding[RDDiagnosis.Systems]
  ): Set[Coding[RDDiagnosis.Systems]] = {

    import Orphanet.extensions._ 

    coding.system match {

      // Case "Orphanet": Include descendant codings and also synonymous ICD-10 codings
      case sys if sys == Coding.System[Orphanet].uri =>
        
        val code =
          coding.asInstanceOf[Coding[Orphanet]].code

        val cs =  
          coding.version
            .flatMap(ordo.get)
            .getOrElse(ordo.latest)

        val concepts =
          cs.concept(code).toSet ++ cs.descendantsOf(code)

        (
          concepts.map(_.toCoding) ++
          concepts
            .flatMap(_.icd10Codes)
            .flatMap(expandDescendantCodings(_))
        )
        .map(_.asInstanceOf[Coding[RDDiagnosis.Systems]])  

      // Case "ICD-10": Include descendant codings and also synonymous Orphanet codings
      case sys if sys == Coding.System[ICD10GM].uri =>

        val icd10codings =
          expandDescendants(coding.asInstanceOf[Coding[ICD10GM]])

        // Combine the ICD-10 descendants coding with Orphanet concepts referencing them
        (
          icd10codings ++ icd10codings.flatMap(c => OrdoByIcd10Index.get(c.code))
        )
        .map(_.asInstanceOf[Coding[RDDiagnosis.Systems]])    

      case _ =>
        Set(
          coding.asInstanceOf[Coding[AlphaIDSE]]
            .complete
            .asInstanceOf[Coding[RDDiagnosis.Systems]]  
        )
    }

  }


  @deprecated("-","")
  val CriteriaExpander: Completer[RDQueryCriteria] = {

    implicit val diseaseSystemsExpander: Completer[Set[Coding[RDDiagnosis.Systems]]] =
      Completer.of(_ flatMap expandEquivalentCodings)


    // Completer to include HPO sub-classes in a query  
    implicit val hpoTermSetCompleter: Completer[Set[Coding[HPO]]] =
      Completer.of {
        _.flatMap(
          hpo =>
            Set(hpo.complete) ++ (
              hpo.code.value match {
                // Little optimization:
                // Given that HP:0000001 is the root class of HPO,
                // skip descendants resolution in this case and directly use all concepts
                case "HP:0000001" => CodeSystem[HPO].concepts

                case _            => CodeSystem[HPO].descendantsOf(hpo.code)
              }
            )
            .map(_.toCoding)
        )
      }

    Completer.of(
      criteria =>
        criteria.copy(
          diagnoses = criteria.diagnoses.complete,
          hpoTerms  = criteria.hpoTerms.complete,
          variants  = criteria.variants.complete
        )
    )

  }

}
