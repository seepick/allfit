package allfit.presentation.tornadofx

import allfit.presentation.models.Rating
import allfit.presentation.renderStars
import javafx.beans.value.ObservableValue
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.Image
import tornadofx.cellFormat
import tornadofx.column
import tornadofx.fixedWidth
import tornadofx.imageview
import tornadofx.label

fun <S> TableView<S>.imageColumn(
    maxWidth: Double,
    padding: Int = 10,
    title: String = "",
    valueProvider: (TableColumn.CellDataFeatures<S, Image>) -> ObservableValue<Image>
) {
    column(title, valueProvider).fixedWidth(maxWidth + padding)
        .cellFormat {
            graphic = imageview(it) {
                fitWidth = maxWidth
                isPreserveRatio = true
            }
        }
}

fun <S> TableView<S>.ratingColumn(
    valueProvider: (TableColumn.CellDataFeatures<S, Rating>) -> ObservableValue<Rating>
) {
    column("Rating", valueProvider)
        .fixedWidth(80)
        .cellFormat { rating ->
            graphic = label(rating.renderStars())
        }
}

fun <T, S> TableView<S>.applyInitSort(column: TableColumn<S, T>) {
    column.isSortable = true
    column.sortType = TableColumn.SortType.ASCENDING
    sortOrder.add(column)
    sort()
}
