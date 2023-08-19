package allfit.presentation.workouts

import allfit.presentation.WorkoutSearchFXEvent
import allfit.presentation.models.FullWorkout
import allfit.presentation.search.CheckinSearchPane
import allfit.presentation.search.DateSearchPane
import allfit.presentation.search.FavoritedSearchPane
import allfit.presentation.search.GroupSearchPane
import allfit.presentation.search.RatingSearchPane
import allfit.presentation.search.ReservedSearchPane
import allfit.presentation.search.SearchRequest
import allfit.presentation.search.SearchView
import allfit.presentation.search.SubSearchRequest
import allfit.presentation.search.TextSearchPane
import allfit.presentation.search.VisitedSearchPane
import allfit.presentation.search.WishlistedSearchPane
import allfit.service.Clock
import tornadofx.FXEvent
import tornadofx.hbox
import tornadofx.vbox

private data object DefaultWorkoutSubSearchRequest : SubSearchRequest<FullWorkout> {
    override val predicate: (FullWorkout) -> Boolean = {
        WorkoutsMainModel.DEFAULT_WORKOUT_PREDICATE(it)
    }

}

class WorkoutsSearchView : SearchView<FullWorkout>(DefaultWorkoutSubSearchRequest) {

    private val clock: Clock by di()

    override val root = hbox(spacing = 30.0) {
        vbox(spacing = 5.0) {
            addIt(VisitedSearchPane(::checkSearch))
            addIt(TextSearchPane(::checkSearch))
            addIt(DateSearchPane(clock, ::checkSearch))
        }
        vbox(spacing = 5.0) {
            addIt(GroupSearchPane(::checkSearch))
            addIt(CheckinSearchPane(::checkSearch))
            addIt(RatingSearchPane(::checkSearch))
        }
        vbox(spacing = 5.0) {
            addIt(WishlistedSearchPane(::checkSearch))
            addIt(FavoritedSearchPane(::checkSearch))
            addIt(ReservedSearchPane(::checkSearch))
        }
    }

    override fun buildEvent(request: SearchRequest<FullWorkout>): FXEvent =
        WorkoutSearchFXEvent(request)

}
