package nl.jacobras.codeobserver.util.ui.notification

import com.gabrieldrn.carbon.notification.NotificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal object Notifier {
    private val scope = CoroutineScope(Dispatchers.Default)
    val notifications = MutableStateFlow<List<Notification>>(emptyList())

    fun show(
        title: String,
        message: String = "",
        status: NotificationStatus
    ) {
        val notification = Notification(
            title = title,
            message = message,
            status = status
        )
        notifications.update { it + notification }
        scope.launch {
            delay(5.seconds)
            dismiss(notification.id)
        }
    }

    fun dismiss(id: String) {
        notifications.update { list ->
            list.filter { it.id != id }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
internal data class Notification(
    val id: String = Uuid.random().toString(),
    val time: Instant = Clock.System.now(),
    val title: String,
    val message: String,
    val status: NotificationStatus
)