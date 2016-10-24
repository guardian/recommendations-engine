import play.api.GlobalSettings
import controllers.Application

object Global extends GlobalSettings {

  override def onStart(app: play.api.Application): Unit = {
    if (app.mode != play.api.Mode.Test) {
      implicit val system = app.actorSystem
      Application.healthcheck.schedule()
    }
  }

  override def onStop(app: play.api.Application): Unit = {
    if (app.mode != play.api.Mode.Test) {
      implicit val system = app.actorSystem
      Application.healthcheck.unschedule()
    }
  }
}