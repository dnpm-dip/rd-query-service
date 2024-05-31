package de.dnpm.dip.rd.query.api


import de.dnpm.dip.service.auth._
import de.dnpm.dip.service.query.QueryPermissions


object RDPermissions extends QueryPermissions("RD")

class RDPermissionsSPI extends PermissionsSPI
{
  override def getInstance: Permissions =
    RDPermissions
}


object RDRoles extends Roles
{

  import RDPermissions._

  val BasicRDQuerier =
    Role(
      "RDQuerierBasic",
      (permissions - ReadPatientRecord),
      Some("RD: Basis-Such-Rechte (Ergebnis-Zusammenfassungen)")
    )

  val PrivilegedRDQuerier =
    Role(
      "RDQuerierPrivileged",
      permissions,
      Some("RD: Privilegierte Such-Rechte (inkl. Einsicht in Patienten-Akten)")
    )

  override val roles: Set[Role] =
    Set(
      BasicRDQuerier,
      PrivilegedRDQuerier
    )

}


class RDRolesSPI extends RolesSPI
{
  override def getInstance: Roles =
    RDRoles
}

