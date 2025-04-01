package com.example.aurora.notifications.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aurora.data.model.WeatherAlertSettings
import com.example.aurora.notifications.NotificationsViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAlertBottomSheet(
    onDismiss: () -> Unit,
    viewModel: NotificationsViewModel
) {
    var useDefaultSound by remember { mutableStateOf(true) }
    val timePickerState = rememberTimePickerState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Schedule Weather Alert",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            TimePicker(state = timePickerState)

//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Use Default Sound",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//                Spacer(modifier = Modifier.weight(1f))
//                Switch(
//                    checked = useDefaultSound,
//                    onCheckedChange = { useDefaultSound = it }
//                )
//            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val duration = calculateDuration(timePickerState)
                    viewModel.scheduleAlert(
                        WeatherAlertSettings(
                            duration = duration,
                            useDefaultSound = useDefaultSound
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Schedule Alert")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun calculateDuration(timePickerState: TimePickerState): Long {
    val now = Calendar.getInstance()
    val selectedTime = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        set(Calendar.MINUTE, timePickerState.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // If selected time is before current time, add one day
    if (selectedTime.before(now)) {
        selectedTime.add(Calendar.DAY_OF_MONTH, 1)
    }

    return selectedTime.timeInMillis - now.timeInMillis
}