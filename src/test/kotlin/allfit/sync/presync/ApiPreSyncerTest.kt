package allfit.sync.presync

import allfit.api.InMemoryOnefitClient
import allfit.api.models.UsageProductJson
import allfit.api.models.UsageProductRuleJson
import allfit.api.models.usageJsonRoot
import allfit.persistence.domain.InMemoryUsageRepository
import allfit.persistence.domain.UsageEntity
import allfit.service.toUtcLocalDateTime
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class ApiPreSyncerTest : DescribeSpec() {

    private val usageJson = Arb.usageJsonRoot().next()
    private lateinit var preSyncer: PreSyncer
    private lateinit var onefitClient: InMemoryOnefitClient
    private lateinit var usageRepo: InMemoryUsageRepository

    override suspend fun beforeEach(testCase: TestCase) {
        onefitClient = InMemoryOnefitClient()
        usageRepo = InMemoryUsageRepository()
        preSyncer = ApiPreSyncer(
            onefitClient = onefitClient,
            listeners = PreSyncListenerManagerImpl(),
            usageRepo = usageRepo,
        )
    }

    init {
        describe("Given usage JSON") {
            it("When sync Then it is stored") {
                onefitClient.usageJson = usageJson

                preSyncer.sync()

                usageRepo.storedUsage shouldBe UsageEntity(
                    total = usageJson.total,
                    noShows = usageJson.no_shows,
                    from = usageJson.period.display_from.toUtcLocalDateTime(),
                    until = usageJson.period.display_till.toUtcLocalDateTime(),
                    periodCap = usageJson.period.product.findRuleAmountByType(
                        UsageProductRuleJson.Types.PERIOD_CAP
                    ),
                    maxCheckInsOrReservationsPerPeriod = usageJson.period.product.findRuleAmountByType(
                        UsageProductRuleJson.Types.MAX_PER_PERIOD
                    ),
                    totalCheckInsOrReservationsPerDay = usageJson.period.product.findRuleAmountByType(
                        UsageProductRuleJson.Types.TOTAL_PER_DAY
                    ),
                    maxReservations = usageJson.period.product.findRuleAmountByType(
                        UsageProductRuleJson.Types.MAX_RESERVATIONS
                    ),
                )
            }
        }
    }
}

private fun UsageProductJson.findRuleAmountByType(type: String) =
    rules.first { it.type == type }.amount

