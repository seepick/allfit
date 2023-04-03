package allfit.sync

import allfit.api.OnefitClient
import allfit.api.WorkoutSearchParams
import allfit.api.models.WorkoutJson
import allfit.persistence.domain.CheckinsRepository
import allfit.persistence.domain.PartnersRepo
import allfit.persistence.domain.ReservationsRepo
import allfit.persistence.domain.WorkoutEntity
import allfit.persistence.domain.WorkoutsRepo
import allfit.service.ImageStorage
import allfit.service.SystemClock
import allfit.service.WorkoutAndImageUrl
import allfit.service.toUtcLocalDateTime
import allfit.service.workParallel
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger

interface WorkoutsSyncer {
    suspend fun sync()
}

class WorkoutsSyncerImpl(
    private val client: OnefitClient,
    private val workoutsRepo: WorkoutsRepo,
    private val workoutFetcher: WorkoutFetcher,
    private val partnersRepo: PartnersRepo,
    private val imageStorage: ImageStorage,
    private val checkinsRepository: CheckinsRepository,
    private val reservationsRepo: ReservationsRepo,
    private val syncListeners: SyncListenerManager
) : WorkoutsSyncer {

    private val log = logger {}
    private val parallelFetchers = 6

    override suspend fun sync() {
        log.debug { "Syncing workouts..." }
        val workoutsToBeSyncedJson = getWorkoutsToBeSynced()
        log.debug { "Fetching metadata for ${workoutsToBeSyncedJson.size} workouts." }
        syncListeners.onSyncDetail("Fetching metadata for ${workoutsToBeSyncedJson.size} workouts.")
        val metaFetchById = mutableMapOf<Int, WorkoutFetch>()
        workoutsToBeSyncedJson.workParallel(parallelFetchers) { workout ->
            metaFetchById[workout.id] = workoutFetcher.fetch(
                WorkoutUrl(
                    workoutId = workout.id,
                    workoutSlug = workout.slug
                )
            )
            delay(30) // artificial delay to soothen possible cloudflare's DoS anger :)
        }

        workoutsRepo.insertAll(workoutsToBeSyncedJson.map { it.toWorkoutEntity(metaFetchById[it.id]!!) })
        imageStorage.saveWorkoutImages(metaFetchById.values
            .filter { it.imageUrls.isNotEmpty() }
            .map { WorkoutAndImageUrl(it.workoutId, it.imageUrls.first()) })


        val startDeletion = SystemClock.todayBeginOfDay().toUtcLocalDateTime()
        reservationsRepo.deleteAllBefore(startDeletion)
        val workoutIdsWithCheckin = checkinsRepository.selectAll().map { it.workoutId }
        val workoutIdsToDelete = workoutsRepo.selectAllBefore(startDeletion)
            .filter { !workoutIdsWithCheckin.contains(it.id) }.map { it.id }
        workoutsRepo.deleteAll(workoutIdsToDelete)
        imageStorage.deleteWorkoutImages(workoutIdsToDelete)
    }

    private suspend fun getWorkoutsToBeSynced(): List<WorkoutJson> {
        val from = SystemClock.todayBeginOfDay()
        val rawWorkouts = client.getWorkouts(WorkoutSearchParams.simple(from = from, plusDays = 14)).data
        val distinctWorkouts = rawWorkouts.distinctBy { it.id }
        if (rawWorkouts.size != distinctWorkouts.size) {
            log.warn { "Dropped ${rawWorkouts.size - distinctWorkouts.size} workouts because of duplicate IDs." }
        }
        val workoutIdsToBeInserted = distinctWorkouts.map { it.id }.toMutableList()
        val entities = workoutsRepo.selectAllStartingFrom(from.toUtcLocalDateTime())
        entities.forEach {
            workoutIdsToBeInserted.remove(it.id)
        }
        val maybeInsertWorkouts = distinctWorkouts.filter { workoutIdsToBeInserted.contains(it.id) }

        // remove all workouts without an existing partner (partner seems disabled, yet workout is being returned)
        val partnerIds = partnersRepo.selectAll().map { it.id }
        return maybeInsertWorkouts.filter {
            val existing = partnerIds.contains(it.partner.id)
            if (!existing) {
                log.warn { "Dropping workout because partner is not known (set inactive by OneFit?!): $it" }
            }
            existing
        }
    }
}

interface WorkoutHtmlMetaData {
    val about: String
    val specifics: String
    val address: String
}

private fun WorkoutJson.toWorkoutEntity(htmlMetaData: WorkoutHtmlMetaData) = WorkoutEntity(
    id = id,
    name = name,
    slug = slug,
    start = from.toUtcLocalDateTime(),
    end = till.toUtcLocalDateTime(),
    partnerId = partner.id,
    about = htmlMetaData.about,
    specifics = htmlMetaData.specifics,
    address = htmlMetaData.address,
)
