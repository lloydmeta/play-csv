package com.beachape.play

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.Form
import play.api.data.Forms._
import org.scalatest.OptionValues._
import org.scalatest.EitherValues._

class CsvSpec extends AnyFunSpec with Matchers {

  describe("query param binding") {

    val subject = Csv.queryStringBindable[Int]

    it("should create a binder that can bind strings corresponding the proper type") {
      subject.bind("hello", Map("hello" -> Seq("1,2,3"))).value.value shouldBe Csv(1, 2, 3)
      subject.bind("hello", Map("hello" -> Seq("1"))).value.value shouldBe Csv(1)
    }

    it("should create a binder that cannot bind strings that don't correspond to the proper type") {
      subject.bind("hello", Map("hello" -> Seq("this is the song that never ends, yes 1t goes on and on my friend"))).value should be(Symbol("left"))
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
      subject.bind("hello", "1,2,3").value shouldBe Csv(1, 2, 3)
      subject.bind("hello", "1").value shouldBe Csv(1)
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
      r1.value.value shouldBe Csv(1, 2, 3)
    }

    it("should fail to bind random strings") {
      val r = Seq(
        subject.bind(Map("hello" -> "AARSE")).value,
        subject.bind(Map("hello" -> "1,A,B")).value,
        subject.bind(Map("hello" -> "99.9, 3, 33")).value)
      r.forall(_ == None) shouldBe true
    }

    it("should unbind") {
      val r1 = subject.mapping.unbind(Csv(1, 2, 3))
      val r2 = subject.mapping.unbind(Csv(1))
      r1 shouldBe Map("hello" -> "1,2,3")
      r2 shouldBe Map("hello" -> "1")

    }
  }

}
