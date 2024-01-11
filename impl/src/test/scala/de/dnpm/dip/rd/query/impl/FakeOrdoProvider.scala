package de.dnpm.dip.rd.query.impl


import cats.Applicative
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  Version
}
import de.dnpm.dip.rd.model.Orphanet



class FakeOrphanetProviderSPI extends Orphanet.OrdoSPI
{
  def getInstance[F[_]]: Orphanet.Ordo[F,Applicative[F]] =
    new FakeOrphanetProvider.Facade[F]
}


object FakeOrphanetProvider
{

  private val codeSystem: CodeSystem[Orphanet] =
    CodeSystem[Orphanet](
      name = "Orphanet-Ordo",
      title = Some("Orphanet Rare Disease Ontology"),
      version = Some("4.3"),
      "ORPHA:98907" -> "Neutrale Lipidspeicherkrankheit mit Ichthyose",
      "ORPHA:98897" -> "Myopathie, okulo-pharyngo-distale",
      "ORPHA:98408" -> "Anämie, megaloblastische, Folat-abhängig",
      "ORPHA:98375" -> "Anämie, autoimmun-hämolytische",
      "ORPHA:984"   -> "Lungenagenesie"

    )


  private [impl] class Facade[F[_]] extends Orphanet.Ordo[F,Applicative[F]]
  {

    import cats.syntax.functor._
    import cats.syntax.applicative._

    override val uri =
      Coding.System[Orphanet].uri


    override val versionOrdering: Ordering[String] =
      Version.Unordered


    override def versions(
      implicit F: Applicative[F]
    ): F[NonEmptyList[String]] =
      latest.map(cs => NonEmptyList.one(cs.version.get))

    override def latestVersion(
      implicit F: Applicative[F]
    ): F[String] =
      versions.map(_.head)

    override def filters(
      implicit env: Applicative[F]
    ): F[List[CodeSystem.Filter[Orphanet]]] =
      List.empty.pure

    override def get(
      version: String
    )(
      implicit F: Applicative[F]
    ): F[Option[CodeSystem[Orphanet]]] =
      latest.map(Some(_))

    override def latest(
      implicit F: Applicative[F]
    ): F[CodeSystem[Orphanet]] =
      codeSystem.pure

  }


}
