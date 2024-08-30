package de.dnpm.dip.rd.query.api


import de.dnpm.dip.rd.model.RDPatientRecord
import de.dnpm.dip.service.query.UseCaseConfig


sealed trait RDConfig extends UseCaseConfig
{

  type PatientRecord = RDPatientRecord

  type Criteria = RDQueryCriteria

//  type Filter = RDFilters

  type Results = RDResultSet
}
