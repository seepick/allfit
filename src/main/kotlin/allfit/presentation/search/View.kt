package allfit.presentation.search

import javafx.event.EventTarget
import javafx.scene.control.CheckBox
import javafx.scene.layout.HBox
import tornadofx.*

abstract class SearchPane : View() {

    abstract var searchFieldPane: SearchFieldPane
    override val root get() = searchFieldPane

    protected abstract fun buildSearchRequest(): SubSearchRequest?

    fun maybeBuildSearchRequest(): SubSearchRequest? =
        if (searchFieldPane.isEnabled) buildSearchRequest() else null
}

class OnEnabledAction(val listener: () -> Unit)

class SearchFieldPane : HBox() {
    private var enabledCheckbox: CheckBox by singleAssign()
    val isEnabled get() = enabledCheckbox.isSelected
    var title: String by property("")
    private fun titleProperty() = getProperty(SearchFieldPane::title)
    var enabledAction: OnEnabledAction by property(OnEnabledAction({}))

    init {
        enabledCheckbox = checkbox {
            action {
                enabledAction.listener()
            }
        }
        label {
            bind(titleProperty())
        }
    }
}

fun EventTarget.searchField(op: SearchFieldPane.() -> Unit = {}): SearchFieldPane {
    val searchField = SearchFieldPane()
    return opcr(this, searchField, op)
}
