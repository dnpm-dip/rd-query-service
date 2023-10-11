package de.dnpm.dip.rd.query.impl


import scala.concurrent.Future
import cats.Monad
import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.rd.query.api.RDCriteria
import de.dnpm.dip.service.query.InMemLocalDB



class InMemRDLocalDB
(
  strict: Boolean
)
extends InMemLocalDB[
  Future,
  Monad,
  RDCriteria,
  RDPatientRecord
](
  RDCriteriaOps.criteriaMatcher(strict = strict)
)
with RDLocalDB

