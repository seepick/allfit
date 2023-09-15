package allfit.sync.domain

import allfit.AppConstants
import allfit.api.OnefitClient
import allfit.api.WorkoutSearchParams
import allfit.api.models.WorkoutJson
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.SinglesRepo
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.Clock
import allfit.service.InsertWorkout
import allfit.service.WorkoutInserter
import allfit.service.toUtcLocalDateTime
import allfit.service.toWorkoutInsertListener
import allfit.sync.core.SyncListenerManager
import io.github.oshai.kotlinlogging.KotlinLogging.logger

interface WorkoutsSyncer {
    suspend fun sync()
}

class WorkoutsSyncerImpl(
    private val client: OnefitClient,
    private val workoutsRepo: WorkoutsRepo,
    private val partnersRepo: PartnersRepo,
    private val checkinsRepository: CheckinsRepository,
    private val reservationsRepo: ReservationsRepo,
    private val clock: Clock,
    private val workoutInserter: WorkoutInserter,
    private val syncListeners: SyncListenerManager,
    private val singlesRepo: SinglesRepo,
) : WorkoutsSyncer {

    private val log = logger {}
    private val syncDaysIntoFuture = AppConstants.workoutsIntoFuture

    override suspend fun sync() {
        log.debug { "Syncing workouts..." }
        workoutInserter.insert(
            workouts = getWorkoutsToBeSynced().map { it.toInsertWorkout() },
            listener = syncListeners.toWorkoutInsertListener()
        )
        deleteOutdated()
    }

    private suspend fun getWorkoutsToBeSynced(): List<WorkoutJson> {
        val locallyNotExistingWorkouts = getLocallyNotExistingWorkouts()

        // remove all workouts without an existing partner (partner seems disabled, yet workout is being returned)
        val partnerIds = partnersRepo.selectAllIds()
        return locallyNotExistingWorkouts.filter { workout ->
            val partnerForWorkoutExistsLocally = partnerIds.contains(workout.partner.id)
            if (!partnerForWorkoutExistsLocally) {
                log.warn { "Dropping workout because partner is not known (set inactive by OneFit?!): $workout" }
            }
            partnerForWorkoutExistsLocally
        }
    }

    private suspend fun getLocallyNotExistingWorkouts(): List<WorkoutJson> {
        val distinctRemoteWorkouts = getDistinctRemoteWorkouts()
        val nonExistingRemoteWorkoutIds = distinctRemoteWorkouts.map { it.id }.toMutableList()
        val existingWorkoutIds = workoutsRepo.selectAllIds()
        nonExistingRemoteWorkoutIds.removeAll(existingWorkoutIds)
        return distinctRemoteWorkouts.filter { nonExistingRemoteWorkoutIds.contains(it.id) }
    }

    private suspend fun getDistinctRemoteWorkouts(): List<WorkoutJson> {
        val rawClientWorkouts = client.getWorkouts(
            WorkoutSearchParams(
                location = singlesRepo.selectLocation(),
                from = clock.todayBeginOfDay(),
                plusDays = syncDaysIntoFuture
            )
        )
        val distinctClientWorkouts = rawClientWorkouts.distinctBy { it.id }
        if (rawClientWorkouts.size != distinctClientWorkouts.size) {
            log.warn { "Dropped ${rawClientWorkouts.size - distinctClientWorkouts.size} workouts because of duplicate IDs." }
        }
        return distinctClientWorkouts
    }

    private fun deleteOutdated() {
        reservationsRepo.deleteAllBefore(clock.now().toUtcLocalDateTime())
        val workoutIdsWithCheckin = checkinsRepository.selectAll().map { it.workoutId }
        val workoutIdsToDelete = workoutsRepo.selectAllBefore(clock.todayBeginOfDay().toUtcLocalDateTime())
            .filter { !workoutIdsWithCheckin.contains(it.id) }.map { it.id }
        log.debug { "Deleting ${workoutIdsToDelete.size} outdated workouts." }
        workoutsRepo.deleteAll(workoutIdsToDelete)
    }
}

private fun WorkoutJson.toInsertWorkout(): InsertWorkout = InsertWorkout(
    id = id,
    partnerId = partner.id,
    slug = slug,
    name = name,
    from = from,
    till = till,
)
