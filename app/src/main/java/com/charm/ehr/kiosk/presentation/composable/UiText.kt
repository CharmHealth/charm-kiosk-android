
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    data class StrResId(
        val id: Int,
        val args: Array<Any> = arrayOf()
    ) : UiText
    data class PluralStrResId(
        val id: Int,
        val count: Int,
        val args: Array<Any> = arrayOf()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is StrResId -> stringResource(id = id, formatArgs = args)
            is PluralStrResId -> pluralStringResource(id = id, count = count, formatArgs = args)
        }
    }
}