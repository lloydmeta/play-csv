package com.beachape.play

import org.apache.commons.lang3.{ StringEscapeUtils, StringUtils }
import play.api.data.Forms._
import play.api.data.format.Formatter
import play.api.data.{ Forms, FormError, Mapping }
import play.api.data.format.Formats
import play.api.libs.json._
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
   * Json [[Reads]] for a Csv of type A
   */
  def csvReads[A: Reads]: Reads[Csv[A]] = new Reads[Csv[A]] {

    def reads(json: JsValue): JsResult[Csv[A]] = json match {
      case JsString(s) => {
        val tryCsvSeq = Try {
          /*
           JsStrings will fail to work with Json.parse here unless explicitly surrounded in quotes,
           making it fairly useless to have Json deseralisation of Csv
            */
          val jsResults = split(s, ',').map(sElem => implicitly[Reads[A]].reads(Json.parse(unescapeCsv(trim(sElem)))))
          val (successes, errors) = jsResults.partition(_.isSuccess)
          if (errors.nonEmpty) {
            errors.foldLeft(JsError(Seq.empty)) { case (JsError(existingE), JsError(nextE)) => JsError(existingE ++ nextE) }
          } else {
            val boundElems = successes.map { case JsSuccess(r, _) => r }
            val test = JsSuccess(Csv(boundElems: _*))
            test
          }
        }
        tryCsvSeq match {
          case Success(jsResult) => jsResult
          case Failure(e) => JsError(s"Could not bind $json into a Csv")
        }

      }
      case _ => JsError(s"Could not bind $json into a Csv because it is not a string")
    }
  }

  /**
   * Json [[Writes]] for a Csv of type A
   */
  def csvWrites[A: Writes]: Writes[Csv[A]] = new Writes[Csv[A]] {
    def writes(o: Csv[A]): JsValue = {
      val elemJsons = o.toSeq.map { elem =>
        val jsValue = Json.toJson(elem)
        /*
          Likewise, writing JsStrings will result in things being surrounded in quotes, making it fairly ugly
        */
        escapeCsv(jsValue.toString())
      }
      JsString(elemJsons.mkString(","))
    }
  }

  /**
   * Json [[Format]] for a Csv of type A
   */
  implicit def formats[A](implicit elemWriter: Writes[A], elemReader: Reads[A]): Format[Csv[A]] = Format(csvReads[A], csvWrites[A])

  /**
   * Given a mapping for a Play Form, returns one that works with [[Csv]]
   *
   * Pretty useless..just stick with [[seq]] unless if you really want to have a [[Csv]]
   *
   * Example:
   * {{{
   * Form("hello" -> Csv.mapping(number))
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
        case Failure(e) => Left(s"Could not bind $value into a Csv")
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
        case _ => Left(Seq(FormError(key, "Could not bind Csv", Nil)))
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