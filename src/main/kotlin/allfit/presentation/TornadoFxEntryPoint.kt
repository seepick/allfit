package allfit.presentation

import allfit.presentation.components.ErrorDialog
import allfit.presentation.logic.MainController
import allfit.presentation.logic.PartnerUpdateController
import allfit.presentation.logic.PrefsController
import allfit.presentation.partnersview.PartnersViewController
import allfit.presentation.view.MainView
import allfit.sync.view.SyncController
import javafx.application.Platform
import javafx.stage.Stage
import mu.KotlinLogging.logger
import tornadofx.App
import tornadofx.find

class TornadoFxEntryPoint : App(
    primaryView = MainView::class,
    stylesheet = Styles::class,
) {
    private val log = logger {}

    init {
        registerEagerSingletons()
    }

    override fun start(stage: Stage) {
        log.info { "start(stage)" }
        super.start(stage)
        find<PrefsController>().prepareStage(stage)
        fire(ApplicationStartedFxEvent)

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            Platform.runLater {
                ErrorDialog.show(throwable)
            }
        }
    }

    private fun registerEagerSingletons() {
        find(MainController::class)
        find(PartnerUpdateController::class)
        find(PartnersViewController::class)
        find(SyncController::class)
    }
}
