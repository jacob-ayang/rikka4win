package me.rerere.rikkahub.data.ai.transformers

import android.content.Context
import android.os.BatteryManager
import android.os.Build
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.datastore.SettingsStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.Temporal
import java.util.Locale
import java.util.TimeZone

object PlaceholderTransformer : InputMessageTransformer, KoinComponent {
    override suspend fun transform(
        ctx: TransformerContext,
        messages: List<UIMessage>,
    ): List<UIMessage> {
        val settingsStore = get<SettingsStore>()
        return messages.map { message ->
            message.copy(
                parts = message.parts.map { part ->
                    if (part is UIMessagePart.Text) {
                        part.copy(
                            text = replacePlaceholders(
                                text = part.text,
                                ctx = ctx,
                                settingsStore = settingsStore
                            )
                        )
                    } else {
                        part
                    }
                }
            )
        }
    }

    private fun replacePlaceholders(
        text: String,
        ctx: TransformerContext,
        settingsStore: SettingsStore,
    ): String {
        val placeholders = mapOf(
            "cur_date" to LocalDate.now().toDateString(),
            "cur_time" to LocalTime.now().toTimeString(),
            "cur_datetime" to LocalDateTime.now().toDateTimeString(),
            "model_id" to ctx.model.modelId,
            "model_name" to ctx.model.displayName,
            "locale" to Locale.getDefault().displayName,
            "timezone" to TimeZone.getDefault().displayName,
            "system_version" to "Android SDK v${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})",
            "device_info" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "battery_level" to ctx.context.batteryLevel().toString(),
            "nickname" to settingsStore.settingsFlow.value.displaySetting.userNickname.ifBlank { "user" },
            "char" to ctx.assistant.name.ifBlank { "assistant" },
            "user" to settingsStore.settingsFlow.value.displaySetting.userNickname.ifBlank { "user" },
        )

        var result = text
        placeholders.forEach { (key, value) ->
            result = result
                .replace(oldValue = "{{$key}}", newValue = value, ignoreCase = true)
                .replace(oldValue = "{$key}", newValue = value, ignoreCase = true)
        }

        return result
    }

    private fun Temporal.toDateString() = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(this)

    private fun Temporal.toTimeString() = DateTimeFormatter
        .ofLocalizedTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(this)

    private fun Temporal.toDateTimeString() = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
        .format(this)

    private fun Context.batteryLevel(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
