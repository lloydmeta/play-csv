package controllers

import com.beachape.play.Csv
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{ Action, Controller }
import play.twirl.api.Html

object Application extends Controller {

  val form = Form("ids" -> Csv.mapping(number))

  def queryParams(ids: Csv[Int]) = Action {
    Ok(Html(
      s"""
        |<html>
        |<body>
        | <h1>Query Params</h1>
        | <p>ids: ${ids.toSeq.mkString(",")}</p>
        |</body>
        |</html>
      """.stripMargin))
  }

  def pathParams(ids: Csv[Int]) = Action {
    Ok(Html(
      s"""
         |<html>
         |<body>
         | <h1>Path Params<h1>
         | <p>ids: ${ids.toSeq.mkString(",")}</p>
         |</body>
         |</html>
      """.stripMargin))
  }

  def showForm = Action {
    import views.html.helper
    Ok(Html(
      s"""
      |<html>
      |<body>
      | ${
        helper.form(controllers.routes.Application.postForm) {
          Html(
            s"""
               |${helper.inputText(form("ids"))}
               |<button type='submit'>Submit</button>""".stripMargin
          )
        }
      }
      |
      |</body>
      |</html>
    """.stripMargin
    ))
  }

  def postForm = Action { implicit r =>
    form.bindFromRequest().fold(
      _ => BadRequest(Html(
        """
          | <html>
          | <body>
          |   <h1>Something went wrong with binding the CSV<h1>
          | </body>
          | </html>
        """.stripMargin)),
      data => Ok(Html(
        s"""
          |<html>
          |<body>
          | <h1>Bound</h1>
          | <p>${data.toSeq.mkString(",")}</p>
          |</body>
          |</html>
        """.stripMargin))
    )
  }

}