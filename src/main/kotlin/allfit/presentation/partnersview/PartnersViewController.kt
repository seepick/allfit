package allfit.presentation.partnersview

import allfit.persistence.domain.UsageRepository
import allfit.presentation.PartnerSearchFXEvent
import allfit.presentation.PartnerSelectedFXEvent
import allfit.presentation.logic.DataStorage
import allfit.presentation.models.FullPartner
import allfit.presentation.models.PartnersViewModel
import allfit.presentation.models.toUsage
import allfit.presentation.tornadofx.safeSubscribe
import mu.KotlinLogging.logger
import tornadofx.Controller

class PartnersViewController : Controller() {

    private val logger = logger {}
    private val partnersModel: PartnersViewModel by inject()
    private val usageRepo: UsageRepository by di()
    private val dataStorage: DataStorage by di()

    init {
        val usage = usageRepo.selectOne().toUsage()
        partnersModel.selectedPartner.initPartner(FullPartner.prototype, usage)

        safeSubscribe<PartnerSelectedFXEvent>() {
            val partnerId = it.partnerId
            logger.debug { "Change partner: $partnerId" }
            partnersModel.selectedPartner.initPartner(dataStorage.getPartnerById(partnerId), usage)
        }
        safeSubscribe<PartnerSearchFXEvent>() {
            logger.debug { "Search: ${it.searchRequest}" }
            partnersModel.sortedFilteredPartners.predicate = it.searchRequest.predicate
        }
    }
}
