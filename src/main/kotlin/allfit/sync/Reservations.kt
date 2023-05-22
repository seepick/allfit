package allfit.sync

import allfit.api.OnefitClient
import allfit.api.models.ReservationJson
import allfit.persistence.domain.ReservationEntity
import allfit.persistence.domain.ReservationsRepo
import allfit.service.Clock
import allfit.service.toUtcLocalDateTime
import mu.KotlinLogging.logger
import java.util.*

interface ReservationsSyncer {
    suspend fun sync()
}

class ReservationsSyncerImpl(
    private val client: OnefitClient,
    private val reservationsRepo: ReservationsRepo,
    private val clock: Clock,
) : ReservationsSyncer {
    private val log = logger {}

    override suspend fun sync() {
        log.debug { "Syncing reservations..." }
        val reservationsRemote = client.getReservations()
        val reservationsLocal = reservationsRepo.selectAllStartingFrom(clock.now().toUtcLocalDateTime())

        val toBeInserted = reservationsRemote.data.associateBy { UUID.fromString(it.uuid) }.toMutableMap()
        reservationsLocal.forEach {
            toBeInserted.remove(it.uuid)
        }

        val toBeDeleted = reservationsLocal.associateBy { it.uuid.toString() }.toMutableMap()
        reservationsRemote.data.forEach {
            toBeDeleted.remove(it.uuid)
        }
        reservationsRepo.insertAll(toBeInserted.values.map { it.toReservationEntity() })
        reservationsRepo.deleteAll(toBeDeleted.map { UUID.fromString(it.key) })
    }
}

private fun ReservationJson.toReservationEntity() = ReservationEntity(
    uuid = UUID.fromString(uuid),
    workoutId = workout.id,
    workoutStart = workout.from.toUtcLocalDateTime(),
)
