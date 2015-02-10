package com.beachape.play

import org.apache.commons.lang3.{ StringEscapeUtils, StringUtils }
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ Forms, FormError, Mapping }
import play.api.data.format.Formats
import play.api.mvc.{ PathBindable, QueryStringBindable }

import scala.util.{ Success, Failure, Try }

/**
 * For binding CSV query params without stomping on binding typeclasses for [[Seq]]
 */
case class Csv[+A](toSeq: A*)

/**
 * Companion object for [[Csv]] that holds useful implicits and helper methods
 */
object Csv {

  import StringEscapeUtils.{ escapeCsv, unescapeCsv }
  import StringUtils.{ trim, removeStart, split }

  /**
   * Empty [[Csv]]
   */
  val Empty = Csv[Nothing]()

  /**
   * Given a mapping for a Play Form, returns one that works with [[Csv]]
   *
   * Pretty useless..just stick with [[seq]] unless if you really want to have a [[Csv]]
   *
   * Example:
   * {{{
   * Form("hello" -> CsvSeq.mapping(number))
   * }}}
   */
  def mapping[A](mapping: Mapping[A]): Mapping[Csv[A]] = Forms.of(formatter(mapping))

  /**
   * Implicit for binding [[Csv]] from query params
   */
  implicit def queryStringBindable[A: QueryStringBindable]: QueryStringBindable[Csv[A]] = new QueryStringBindable[Csv[A]] {

    def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Csv[A]]] = {
      if (params.get(key).isEmpty) {
        None
      } else {
        val trySeq = Try {
          for {
            strings <- params.get(key).toSeq
            string <- strings
            rawValue <- split(string, ',')
            bound <- implicitly[QueryStringBindable[A]].bind(key, Map(key -> Seq(unescapeCsv(trim(rawValue)))))
          } yield bound.right.get // This will throw if not all strings are bindable
        }
        trySeq match {
          case Success(seq) => Some(Right(Csv(seq: _*)))
          case Failure(e) => Some(Left(s"Failed to bind all of ${params.get(key)} "))
        }
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

  /**
   * Implicit for binding [[Csv]] from path params
   */
  implicit def pathStringBindable[A: PathBindable]: PathBindable[Csv[A]] = new PathBindable[Csv[A]] {

    def bind(key: String, value: String): Either[String, Csv[A]] = {
      val tryCsvSeq = Try {
        val seq = for {
          rawValue <- split(value, ',')
        } yield implicitly[PathBindable[A]].bind(key, unescapeCsv(trim(rawValue))).right.get
        Csv(seq: _*)
      }
      tryCsvSeq match {
        case Success(csv) => Right(csv)
        case Failure(e) => Left(s"Could not bind $value into a CsvSeq")
      }
    }

    def unbind(key: String, value: Csv[A]): String = {
      val elemStrings = value.toSeq.map { v =>
        escapeCsv(implicitly[PathBindable[A]].unbind(key, v))
      }
      elemStrings.mkString(",")
    }
  }

  private def formatter[A](mapping: Mapping[A]): Formatter[Csv[A]] = new Formatter[Csv[A]] {

    def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Csv[A]] = {
      val elemBinder = mapping.withPrefix(key)
      val tryBind = Try {
        Formats.stringFormat.bind(key, data).right.map { s =>
          val seq = split(s, ',') map { p => elemBinder.bind(Map(key -> unescapeCsv(trim(p)))).right.get }
          Csv(seq: _*)
        }
      }
      tryBind match {
        case Success(Right(csvSeq)) => Right(csvSeq)
        case _ => Left(Seq(FormError(key, "Could not bind CsvSeq", Nil)))
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

}