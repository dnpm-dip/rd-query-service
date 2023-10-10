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
  RDCriteria
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
          category = diag.category.complete, 
          status   = diag.status.complete
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
          gene              = v.gene.complete,
          acmgClass         = v.acmgClass.map(_.complete),
          zygosity          = v.zygosity.map(_.complete),
          deNovo            = v.deNovo.map(_.complete),
          modeOfInheritance = v.modeOfInheritance.map(_.complete),
          significance      = v.significance.map(_.complete),
        )
    )


  implicit val ngsReportCompleter: Completer[RDNGSReport] =
    Completer.of(
      ngs =>
        ngs.copy(
          variants = ngs.variants.map(_.map(_.complete))
        )
    )


  implicit val rdPatientRecordCompleter: Completer[RDPatientRecord] =
    Completer.of(
      patRec =>
        patRec.copy(
          patient   = patRec.patient.complete,
          diagnosis = patRec.diagnosis.complete,
          hpoTerms  = patRec.hpoTerms.map(_.map(_.complete)),
          ngsReport = patRec.ngsReport.complete
        )
  )


  implicit val criteriaCompleter: Completer[RDCriteria] = {

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

