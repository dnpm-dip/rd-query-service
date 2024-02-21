package de.dnpm.dip.rd.query.api


import de.dnpm.dip.service.auth.{
  Permission,
  Permissions,
  PermissionsSPI,
}
import de.dnpm.dip.service.query.QueryPermissions


object RDPermissions extends QueryPermissions("RD")

class RDPermissionsSPI extends PermissionsSPI
{
  override def getInstance: Permissions =
    RDPermissions
}


