package de.dnpm.dip.rd.query.api


import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers._
import org.scalatest.Inspectors._
import de.dnpm.dip.service.auth.Permissions


class PermissionTests extends AnyFlatSpec
{

  val spiTry = 
    Permissions.getInstance


  "PermissionSPI" must "have worked" in {

    spiTry.isSuccess mustBe true
   
  }


  "Permission set" must "be non-empty" in {

    spiTry.get.permissions must not be (empty)

  }

  "Pattern matching of submission names" must "have been successful" in {

    RDPermissions
      .permissions
      .map(_.name)
      .collect { case RDPermissions(p) => p }
      .toSet must equal (RDPermissions.values.toSet)

  }

}
