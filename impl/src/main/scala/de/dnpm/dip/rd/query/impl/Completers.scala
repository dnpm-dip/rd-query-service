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
  CodeSystemProvider
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.coding.hgvs.HGVS
import de.dnpm.dip.coding.icd.ICD10GM
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


trait Completers
{

  import Completer.syntax._

  import scala.util.chaining._ 



  implicit val hpOntology: CodeSystem[HPO]

  implicit val hgnc: CodeSystem[HGNC]

  implicit val ordo: CodeSystem[Orphanet]

  implicit val omim: CodeSystem[OMIM]

  implicit val icd10gm: CodeSystemProvider[ICD10GM,Id,Applicative[Id]]


  implicit val patientCompleter: Completer[Patient] =
    Completer.of(
      pat =>
        pat.copy(
          gender       = pat.gender.complete,
          managingSite = Some(Site.local)
        )
    )


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


/*
  implicit val diagCategoryCompleter: Completer[Coding[RDDiagnosis.Category]] = 
    Completer.of { 
      coding =>
        (
          coding.system match {
            case sys if sys == Coding.System[Orphanet].uri =>
              coding.asInstanceOf[Coding[Orphanet]].complete
            
            case sys if sys == Coding.System[OMIM].uri =>
              coding.asInstanceOf[Coding[OMIM]].complete
            
            case sys if sys == Coding.System[ICD10GM].uri =>
              coding.asInstanceOf[Coding[ICD10GM]].complete
          }
        )
        .asInstanceOf[Coding[RDDiagnosis.Category]]

    }
*/


  implicit val diagnosisCompleter: Completer[RDDiagnosis] =
    Completer.of(
      diag =>
        diag.copy(
          categories = diag.categories.complete, 
          status     = diag.status.complete
        )
    )


  implicit val hpoTermCompleter: Completer[HPOTerm] =
    Completer.of(
      hpo =>
        hpo.copy(
          value = hpo.value.complete
        )
    )


  implicit val acmgCriterionCompleter: Completer[ACMG.Criterion] =
    Completer.of(
      acmg =>
        acmg.copy(
          value    = acmg.value.complete,
          modifier = acmg.modifier.complete
        )
    )


  implicit val smallVariantCompleter: Completer[SmallVariant] =
    Completer.of(
      v =>
        v.copy(
          genes               = v.genes.complete,
          acmgClass           = v.acmgClass.complete,
          acmgCriteria        = v.acmgCriteria.complete,
          zygosity            = v.zygosity.complete,
          segregationAnalysis = v.segregationAnalysis.complete,
          modeOfInheritance   = v.modeOfInheritance.complete,
          significance        = v.significance.complete,
        )
    )


  implicit val structuralVariantCompleter: Completer[StructuralVariant] =
    Completer.of(
      v =>
        v.copy(
          genes               = v.genes.complete,
          acmgClass           = v.acmgClass.complete,
          acmgCriteria        = v.acmgCriteria.complete,
          zygosity            = v.zygosity.complete,
          segregationAnalysis = v.segregationAnalysis.complete,
          modeOfInheritance   = v.modeOfInheritance.complete,
          significance        = v.significance.complete,
        )
    )


  implicit val copyNumberVariantCompleter: Completer[CopyNumberVariant] =
    Completer.of(
      v =>
        v.copy(
          genes               = v.genes.complete,
          acmgClass           = v.acmgClass.complete,
          acmgCriteria        = v.acmgCriteria.complete,
          zygosity            = v.zygosity.complete,
          segregationAnalysis = v.segregationAnalysis.complete,
          modeOfInheritance   = v.modeOfInheritance.complete,
          significance        = v.significance.complete,
        )
    )


  implicit val ngsReportCompleter: Completer[RDNGSReport] =
    Completer.of(
      ngs =>
        ngs.copy(
          smallVariants      = ngs.smallVariants.complete,
          structuralVariants = ngs.structuralVariants.complete,
          copyNumberVariants = ngs.copyNumberVariants.complete
        )
    )


  implicit val rdPatientRecordCompleter: Completer[RDPatientRecord] =
    Completer.of(
      patRec =>
        patRec.copy(
          patient    = patRec.patient.complete,
          diagnosis  = patRec.diagnosis.complete,
          hpoTerms   = patRec.hpoTerms.complete,
          ngsReports = patRec.ngsReports.complete
        )
    )


  implicit val variantCriteriaCompleter: Completer[VariantCriteria] = {

    val proteinChangeCompleter: Completer[Coding[HGVS]] =
      Completer.of {
        coding =>
          val threeLetterCode = HGVS.Protein.to3LetterCode(coding.code.value)
          coding.copy(
            code = Code[HGVS](threeLetterCode),
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

  
  val CriteriaExpander: Completer[RDQueryCriteria] = {

    // Completer to include Orpha sub-classes in a query  
    implicit val orphanetCodingSetCompleter: Completer[Set[Coding[Orphanet]]] =
      Completer.of {
        _.flatMap {
          orpha =>
            Set(orpha.complete) ++
              ordo.descendantsOf(orpha.code)
                .map(_.toCoding)
        
        }
      }

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
                  CodeSystem[HPO]
                    .descendantsOf(hpo.code)
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
