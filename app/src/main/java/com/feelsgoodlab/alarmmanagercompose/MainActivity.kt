package com.feelsgoodlab.alarmmanagercompose

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.feelsgoodlab.alarmmanagercompose.ui.theme.AlarmManagerComposeTheme
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var alarmReceiver: AlarmReceiver

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission rejected", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        alarmReceiver = AlarmReceiver()

        enableEdgeToEdge()
        setContent {
            AlarmManagerComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        item {
                            OneTime(modifier = Modifier.fillMaxWidth()) { context, date, time, message ->
                                alarmReceiver.setOneTimeAlarm(
                                    context,
                                    AlarmReceiver.TYPE_ONE_TIME,
                                    date,
                                    time,
                                    message
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneTime(
    modifier: Modifier = Modifier,
    onSetOneTime: (context: Context, selectedDate: String, selectedTime: String, alarmDescription: String) -> Unit
) {
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedTime: TimePickerState? by rememberSaveable { mutableStateOf(null) }
    var isDateModalShown by rememberSaveable { mutableStateOf(false) }
    var isClockModalShown by rememberSaveable { mutableStateOf(false) }
    var alarmDescription by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text("One time alarm")

        if (isDateModalShown) {
            DatePickerModal(onDateSelected = {
                selectedDate = it
                isDateModalShown = false
            }, onDismiss = {
                isDateModalShown = false
            })
        }

        if (isClockModalShown) {
            ClockPickerModal(onClockSelected = { it ->
                selectedTime = it
                isClockModalShown = false
            }, onDismiss = {
                isClockModalShown = false
            })
        }

        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    isDateModalShown = true
                }) {
                Icon(Icons.Filled.DateRange, contentDescription = "Pick a Date")
            }
            if (selectedDate != null) {
                val date = Date(selectedDate!!)
                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                Text("Selected date: $formattedDate")
            } else {
                Text("No date selected")
            }
        }
        Row(
            modifier = Modifier.padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                modifier = Modifier.padding(end = 16.dp),
                onClick = { isClockModalShown = true }) {
                Icon(
                    painter = painterResource(R.drawable.clock),
                    contentDescription = "Pick a clock"
                )
            }
            if (selectedTime != null) {
                val numFormat: NumberFormat = DecimalFormat("00")
                Text(
                    "Selected time: ${numFormat.format(selectedTime?.hour)}:${
                        numFormat.format(
                            selectedTime?.minute
                        )
                    }"
                )
            } else {
                Text("No time selected")
            }
        }
        TextField(
            modifier = Modifier.padding(vertical = 8.dp),
            value = alarmDescription,
            onValueChange = { alarmDescription = it },
            label = { Text("Description") })
        Button(onClick = {
            val numFormat: NumberFormat = DecimalFormat("00")
            val date = Date(selectedDate!!)
            val formattedDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

            onSetOneTime(
                context,
                formattedDate,
                "${numFormat.format(selectedTime?.hour)}:${
                    numFormat.format(
                        selectedTime?.minute
                    )
                }",
                alarmDescription
            )
        }) {
            Text("Set alarm")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        Button(onClick = {
            onDateSelected(datePickerState.selectedDateMillis)
        }) {
            Text("Confirm")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }) {
        DatePicker(datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClockPickerModal(onClockSelected: (TimePickerState) -> Unit, onDismiss: () -> Unit) {

    val currentClock = Calendar.getInstance()
    val clockPickerState = rememberTimePickerState(
        initialHour = currentClock.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentClock.get(Calendar.MINUTE),
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onClockSelected(clockPickerState)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text("Pick a clock")
        },
        text = {
            TimePicker(clockPickerState)
        }
    )
}