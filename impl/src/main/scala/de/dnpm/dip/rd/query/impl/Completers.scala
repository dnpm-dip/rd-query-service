package de.dnpm.dip.rd.query.impl


import de.dnpm.dip.util.Completer
import de.dnpm.dip.model.{
  Patient,
  Observation,
  Site
}
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  CodeSystemProvider
}
import de.dnpm.dip.coding.hgnc.HGNC
import de.dnpm.dip.rd.model.{
  HPO,
  HPOTerm,
  RDDiagnosis,
  RDNGSReport,
  RDPatientRecord,
  Variant
}
import de.dnpm.dip.rd.query.api.{
  DiagnosisCriteria,
  VariantCriteria,
  RDQueryCriteria
}



trait Completers
{

  import Completer.syntax._


  val localSite: Coding[Site]


  implicit val hpOntology: CodeSystem[HPO]

  implicit val hgnc: CodeSystem[HGNC]


  implicit val patientCompleter: Completer[Patient] =
    Completer.of(
      pat =>
        pat.copy(
          gender       = pat.gender.complete,
          managingSite = Some(localSite)
        )
    )


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


  implicit val variantCompleter: Completer[Variant] =
    Completer.of(
      v =>
        v.copy(
          gene                = v.gene.complete,
          acmgClass           = v.acmgClass.complete,
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
          variants = ngs.variants.complete
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


  implicit val criteriaCompleter: Completer[RDQueryCriteria] = {

    implicit val diagCriteriaCompleter: Completer[DiagnosisCriteria] =
      Completer.of(dc =>
        dc.copy(
          category = dc.category.complete,
          status   = dc.status.complete
        )
      )

    implicit val variantCriteriaCompleter: Completer[VariantCriteria] =
      Completer.of(vc =>
        vc.copy(
          gene = vc.gene.complete
        )
      )

    /*
    // TODO:
    // Uncomment when ensured whether HPO sub-classes should be included in a query  
    implicit val hpoTermSetCompleter: Completer[Option[Set[Coding[HPO]]]] =
      Completer.of {
        _.map(
          _.flatMap(
            hpo =>
              Set(hpo.complete) ++
              CodeSystem[HPO]
                .descendantsOf(hpo.code)
                .map(_.toCoding)
          )
        )
      }
    */

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

