package cl.oursour.app

import android.app.Application

/** Clase Application: se crea una vez cuando arranca el proceso de la app. */
class OurSourApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
