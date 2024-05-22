package de.dnpm.dip.rd.query.impl


import cats.{
  Applicative,
  Id
}
import cats.data.NonEmptyList
import de.dnpm.dip.util.Completer
import de.dnpm.dip.model.{
  Patient,
  Observation,
  Site
}
import de.dnpm.dip.coding.{
  Code,
  Coding,
  CodeSystem,
  CodeSystemProvider,
  CodeSystemProviders
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.icd.ICD10GM
import de.dnpm.dip.service.BaseCompleters
import de.dnpm.dip.rd.model.{
  ACMG,
  HPO,
  HPOTerm,
  OMIM,
  Orphanet,
  RDDiagnosis,
  RDNGSReport,
  RDPatientRecord,
  SmallVariant,
  CopyNumberVariant,
  StructuralVariant
}
import de.dnpm.dip.rd.query.api.{
  VariantCriteria,
  RDQueryCriteria
}
import shapeless.{
  Coproduct,
  :+:,
  CNil
}


trait Completers extends BaseCompleters
{

  import Completer.syntax._
  import scala.util.chaining._ 

  implicit val hpOntology: CodeSystem[HPO]

  implicit val hgnc: CodeSystemProvider[HGNC,Id,Applicative[Id]]

  implicit val ordo: CodeSystemProvider[Orphanet,Id,Applicative[Id]]

  implicit val omim: CodeSystemProvider[OMIM,Id,Applicative[Id]]

  implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]


  implicit def coproductCodingCompleter[
    H: Coding.System,
    T <: Coproduct
  ](
    implicit
    compH: Completer[Coding[H]],
    compT: Completer[Coding[T]]
  ): Completer[Coding[H :+: T]] =
    Completer.of { 
      coding =>
        (
          if (coding.system == Coding.System[H].uri)
            compH(coding.asInstanceOf[Coding[H]])
          else
            compT(coding.asInstanceOf[Coding[T]])
        )
        .asInstanceOf[Coding[H :+: T]]
    }

  implicit def terminalCoproductCodingCompleter[
    H: Coding.System
  ](
    implicit
    compH: Completer[Coding[H]],
  ): Completer[Coding[H :+: CNil]] =
    compH.asInstanceOf[Completer[Coding[H :+: CNil]]]


  implicit val variantCriteriaCompleter: Completer[VariantCriteria] = {

    val proteinChangeCompleter: Completer[Coding[HGVS.Protein]] =
      Completer.of {
        coding =>
          val threeLetterCode = HGVS.Protein.to3LetterCode(coding.code.value)
          coding.copy(
            code = Code[HGVS.Protein](threeLetterCode),
            display = coding.display.orElse(Some(threeLetterCode))
          )
      }

    Completer.of(vc =>
      vc.copy(
        gene          = vc.gene.complete,
        proteinChange = vc.proteinChange.map(proteinChangeCompleter)
      )
    )

  }


  implicit val criteriaCompleter: Completer[RDQueryCriteria] = {

    Completer.of(
      criteria =>
        criteria.copy(
          diagnoses = criteria.diagnoses.complete,
          hpoTerms  = criteria.hpoTerms.complete,
          variants  = criteria.variants.complete
        )
    )

  }



  private def expand[T,U >: T](
    coding: Coding[T],
    cs: CodeSystem[U]
  ): Set[Coding[T]] =
    (cs.concept(coding.code).toSet ++ cs.descendantsOf(coding.code))
      .map(
        _.toCoding(coding.system)
         .asInstanceOf[Coding[T]]
      )

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


  val CriteriaExpander: Completer[RDQueryCriteria] = {

    implicit val diseaseCategoryExpander: Completer[Set[Coding[RDDiagnosis.Category]]] =
      descendantExpanderOf[RDDiagnosis.Category]


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
                case "HP:0000001" =>
                  CodeSystem[HPO].concepts

                case x =>
                  CodeSystem[HPO].descendantsOf(hpo.code)
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
