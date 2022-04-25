package com.beachape.play

import org.apache.commons.lang3.StringUtils
import org.apache.commons.text.StringEscapeUtils
import play.api.data.FormError
import play.api.data.Forms
import play.api.data.Mapping
import play.api.data.format.Formats
import play.api.data.format.Formatter
import play.api.mvc.PathBindable
import play.api.mvc.QueryStringBindable

import scala.util.Success
import scala.util.Try

/** For binding CSV query params without stomping on binding typeclasses for
  * [[Seq]]
  */
case class Csv[+A](toSeq: A*)

/** Companion object for [[Csv]] that holds useful implicits and helper methods
  */
object Csv {

  import StringEscapeUtils.escapeCsv
  import StringEscapeUtils.unescapeCsv
  import StringUtils.removeStart
  import StringUtils.split
  import StringUtils.trim

  /** Empty [[Csv]] */
  val Empty = Csv[Nothing]()

  /** Given a mapping for a Play Form, returns one that works with [[Csv]]
    *
    * Pretty useless..just stick with [[seq]] unless if you really want to have
    * a [[Csv]]
    *
    * Example:
    * {{{
    * Form("hello" -> Csv.mapping(number))
    * }}}
    */
  def mapping[A](mapping: Mapping[A]): Mapping[Csv[A]] =
    Forms.of(formatter(mapping))

  /** Implicit for binding [[Csv]] from query params */
  implicit def queryStringBindable[A: QueryStringBindable]
    : QueryStringBindable[Csv[A]] = new QueryStringBindable[Csv[A]] {

    def bind(
      key: String,
      params: Map[String, Seq[String]]
    ): Option[Either[String, Csv[A]]] = {
      if (!params.contains(key)) {
        None
      } else {
        val tryBinds = Try {
          for {
            strings <- params.get(key).toSeq
            string <- strings
            rawValue <- split(string, ',')
            bound <- implicitly[QueryStringBindable[A]]
              .bind(key, Map(key -> Seq(unescapeCsv(trim(rawValue)))))
          } yield bound
        }
        Some(
          transformOrElse(s"Failed to bind all of ${params.get(key)}")(tryBinds)
        )
      }
    }

    def unbind(key: String, value: Csv[A]): String = {
      val elemStrings = value.toSeq.map { v =>
        val unbound = implicitly[QueryStringBindable[A]].unbind(key, v)
        escapeCsv(removeStart(unbound, s"$key="))
      }
      s"$key=${elemStrings.mkString(",")}"
    }
  }

  /** Implicit for binding [[Csv]] from path params
    */
  implicit def pathStringBindable[A: PathBindable]: PathBindable[Csv[A]] =
    new PathBindable[Csv[A]] {

      def bind(key: String, value: String): Either[String, Csv[A]] = {
        val tryBinds = Try {
          split(value, ',').toSeq map (raw =>
            implicitly[PathBindable[A]].bind(key, unescapeCsv(trim(raw)))
          )
        }
        transformOrElse(s"Could not bind $value into a Csv")(tryBinds)
      }

      def unbind(key: String, value: Csv[A]): String = {
        val elemStrings = value.toSeq.map { v =>
          escapeCsv(implicitly[PathBindable[A]].unbind(key, v))
        }
        elemStrings.mkString(",")
      }
    }

  private[this] def formatter[A](mapping: Mapping[A]): Formatter[Csv[A]] =
    new Formatter[Csv[A]] {

      def bind(
        key: String,
        data: Map[String, String]
      ): Either[Seq[FormError], Csv[A]] = {
        val elemBinder = mapping.withPrefix(key)
        val tryEitherSeqEitherBinds = Try {
          Formats.stringFormat.bind(key, data).map { s =>
            split(s, ',').toSeq map { p =>
              elemBinder.bind(Map(key -> unescapeCsv(trim(p))))
            }
          }
        }
        tryEitherSeqEitherBinds match {
          case Success(Right(seqEitherBinds))
              if seqEitherBinds.forall(_.isRight) =>
            transformOrElse(Seq(FormError(key, "Could not bind Csv", Nil)))(
              Success(seqEitherBinds)
            )
          case _ =>
            Left(Seq(FormError(key, "Could not bind Csv", Nil)))
        }
      }

      def unbind(key: String, value: Csv[A]): Map[String, String] = {
        val elemStrings = for {
          v <- value.toSeq
          vString <- mapping.unbind(v).values
        } yield escapeCsv(vString)
        Map(key -> elemStrings.mkString(","))
      }
    }

  // The orElse comes first so we can let the compiler infer types
  private[this] def transformOrElse[A, B](
    orElse: => A
  )(tryBinds: Try[Seq[Either[A, B]]]): Either[A, Csv[B]] = {
    tryBinds match {
      case Success(seqEithers) if seqEithers.forall(_.isRight) =>
        val seq = for {
          either <- seqEithers
          v <- either.toOption
        } yield v
        Right(Csv(seq: _*))
      case _ =>
        Left(orElse)
    }
  }

}
