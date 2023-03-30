package allfit.presentation.view

import allfit.presentation.PresentationConstants
import allfit.presentation.StaticImage
import allfit.presentation.StaticImageStorage
import allfit.presentation.models.FullWorkout
import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import javafx.geometry.Pos
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.image.Image
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.label
import tornadofx.readonlyColumn
import tornadofx.remainingWidth
import tornadofx.smartResize
import tornadofx.weightedWidth

class WorkoutsTable() : TableView<FullWorkout>() {
    init {
        smartResize()

        selectionModel.selectionMode = SelectionMode.SINGLE

        column<FullWorkout, Image>("") { it.value.imageProperty() }
            .fixedWidth(PresentationConstants.tableImageWidth + 10)
            .cellFormat {
                graphic = imageview(it)
            }

        column<FullWorkout, String>("Workout") { it.value.nameProperty() }
            .remainingWidth()
            .weightedWidth(0.5)

        readonlyColumn("Date", FullWorkout::date)
            .fixedWidth(150)
            .cellFormat { date ->
                text = date.prettyString
            }

        column<FullWorkout, Boolean>("Reserved") { it.value.isReservedProperty() }
            .fixedWidth(60)
            .cellFormat { isReserved ->
                if (isReserved) {
                    graphic = imageview(StaticImageStorage.get(StaticImage.Reserved)) {
                        alignment = Pos.CENTER
                    }
                }
            }

        column<FullWorkout, Image>("") { it.value.partner.imageProperty() }
            .fixedWidth(PresentationConstants.tableImageWidth + 10)
            .cellFormat {
                graphic = imageview(it)
            }

        column<FullWorkout, String>("Partner") { it.value.partner.nameProperty() }
            .remainingWidth()
            .weightedWidth(0.5)

        column<FullWorkout, Int>("#") { it.value.partner.checkinsProperty() }
            .fixedWidth(30)

        column<FullWorkout, Rating>("Rating") { it.value.partner.ratingProperty() }
            .fixedWidth(80)
            .cellFormat { rating ->
                graphic = label(rating.renderStars())
            }

        column<FullWorkout, Boolean>("Favorite") { it.value.partner.isFavoritedProperty() }
            .fixedWidth(60)
            .cellFormat { isFavorite ->
                graphic = imageview(StaticImageStorage.get(if (isFavorite) StaticImage.FavoriteFull else StaticImage.FavoriteOutline)) {
                    alignment = Pos.CENTER
                }
            }

        column<FullWorkout, Boolean>("Wishlist") { it.value.partner.isWishlistedProperty() }
            .fixedWidth(60)
            .cellFormat { isWishlisted ->
                graphic = imageview(StaticImageStorage.get(if (isWishlisted) StaticImage.WishlistFull else StaticImage.WishlistOutline)) {
                    alignment = Pos.CENTER
                }
            }
    }
}