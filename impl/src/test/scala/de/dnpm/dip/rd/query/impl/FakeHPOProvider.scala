package de.dnpm.dip.rd.query.impl


import cats.Applicative
import cats.data.NonEmptyList
import de.dnpm.dip.coding.{
  Coding,
  CodeSystem,
  Version
}
import de.dnpm.dip.rd.model.HPO



class FakeHPOProviderSPI extends HPO.OntologySPI
{
  def getInstance[F[_]]: HPO.Ontology[F,Applicative[F]] =
    new FakeHPOProvider.Facade[F]
}


object FakeHPOProvider
{

  private val codeSystem: CodeSystem[HPO] =
    CodeSystem[HPO](
      name = "Human-Phenotype-Ontology",
      title = Some("Human Phenotype Ontology"),
      version = Some("Fake"),
      "HP:0100861" -> "Sclerotic vertebral body",
      "HP:0100863" -> "Aplasia of the femoral neck",
      "HP:0100864" -> "Short femoral neck",
      "HP:0100869" -> "Palmar telangiectasia",
      "HP:0100871" -> "Abnormal palm morphology",
      "HP:0100877" -> "Renal diverticulum"
    )


  private [impl] class Facade[F[_]] extends HPO.Ontology[F,Applicative[F]]
  {

    import cats.syntax.functor._
    import cats.syntax.applicative._

    override val uri =
      Coding.System[HPO].uri


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

    override def get(
      version: String
    )(
      implicit F: Applicative[F]
    ): F[Option[CodeSystem[HPO]]] =
      latest.map(Some(_))

    override def latest(
      implicit F: Applicative[F]
    ): F[CodeSystem[HPO]] =
      codeSystem.pure

  }


}
