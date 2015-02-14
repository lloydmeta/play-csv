package com.beachape.play

import org.scalatest.{ Matchers, FunSpec }
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{ Json, JsNumber, JsString }

class CsvSpec extends FunSpec with Matchers {

  describe("query param binding") {

    val subject = Csv.queryStringBindable[Int]

    it("should create a binder that can bind strings corresponding the proper type") {
      subject.bind("hello", Map("hello" -> Seq("1,2,3"))).get.right.get shouldBe Csv(1, 2, 3)
      subject.bind("hello", Map("hello" -> Seq("1"))).get.right.get shouldBe Csv(1)
    }

    it("should create a binder that cannot bind strings that don't correspond to the proper type") {
      subject.bind("hello", Map("hello" -> Seq("this is the song that never ends, yes 1t goes on and on my friend"))).get should be('left)
      subject.bind("hello", Map("helloz" -> Seq("1.2, 3.4"))) shouldBe None
    }

    it("should create a binder that can unbind values") {
      subject.unbind("hello", Csv(1, 2, 3)) should be("hello=1,2,3")
      subject.unbind("hello", Csv(1)) should be("hello=1")
    }

  }

  describe("path param binding") {

    val subject = Csv.pathStringBindable[Int]

    it("should create a binder that can bind strings corresponding the proper type") {
      subject.bind("hello", "1,2,3").right.get shouldBe Csv(1, 2, 3)
      subject.bind("hello", "1").right.get shouldBe Csv(1)
    }

    it("should create a binder that cannot bind strings that don't correspond to the proper type") {
      subject.bind("hello", "this is the song that never ends, yes 1t goes on and on my friend").isLeft shouldBe true
      subject.bind("hello", "1.2, 3.4").isLeft shouldBe true
    }

    it("should create a binder that can unbind values") {
      subject.unbind("hello", Csv(1, 2, 3)) shouldBe "1,2,3"
      subject.unbind("hello", Csv(1)) shouldBe "1"
    }
  }

  describe("form binding") {

    val subject = Form("hello" -> Csv.mapping(number))

    it("should bind proper strings into a CsvSeq") {
      val r1 = subject.bind(Map("hello" -> "1,2,3"))
      r1.value.get shouldBe Csv(1, 2, 3)
    }

    it("should fail to bind random strings") {
      val r = Seq(
        subject.bind(Map("hello" -> "AARSE")).value,
        subject.bind(Map("hello" -> "1,A,B")).value,
        subject.bind(Map("hello" -> "99.9, 3, 33")).value
      )
      r.forall(_ == None) shouldBe true
    }

    it("should unbind") {
      val r1 = subject.mapping.unbind(Csv(1, 2, 3))
      val r2 = subject.mapping.unbind(Csv(1))
      r1 shouldBe Map("hello" -> "1,2,3")
      r2 shouldBe Map("hello" -> "1")

    }
  }

  describe("Json serdes") {

    it("should deserialise a proper Json CSV") {
      val js1 = JsString("1,2,3")
      val js2 = JsString("a,b,c")
      val des1 = js1.as[Csv[Int]]
      val des2 = js2.as[Csv[String]]
      des1 shouldBe Csv(1, 2, 3)
      des2 shouldBe Csv("a", "b", "c")
    }

    it("should fail to deserialise improper types") {
      val js1 = JsString("a, 1, 3")
      val js2 = JsNumber(99)
      val des1 = js1.asOpt[Csv[Int]]
      val des2 = js2.asOpt[Csv[String]]
      des1 shouldBe None
      des2 shouldBe None
    }

    it("should unbind properly") {
      val seq = Csv("a", "b", "c")
      val js = Json.toJson(seq)
      js shouldBe JsString("a,b,c")
    }

  }

}
