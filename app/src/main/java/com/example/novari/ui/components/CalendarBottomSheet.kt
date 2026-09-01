package com.example.novari.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.novari.ui.screens.transactions.transaction_list.DateFilter
import com.example.novari.ui.theme.NovariColors
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private enum class SelectionMode { SINGLE, RANGE }

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarBottomSheet(
    initialFilter: DateFilter,
    onApply: (DateFilter) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.now()

    var mode by rememberSaveable {
        mutableStateOf(
            if (initialFilter is DateFilter.Range) SelectionMode.RANGE else SelectionMode.SINGLE
        )
    }

    var rangeStart by rememberSaveable {
        mutableStateOf(
            when (initialFilter) {
                is DateFilter.Single -> initialFilter.date
                is DateFilter.Range -> initialFilter.start
                DateFilter.None -> today
            }
        )
    }

    var rangeEnd by rememberSaveable {
        mutableStateOf(if (initialFilter is DateFilter.Range) initialFilter.end else null)
    }

    val startMonth = currentMonth.minusMonths(12)

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = currentMonth,
        firstVisibleMonth = YearMonth.from(rangeStart),
        firstDayOfWeek = DayOfWeek.MONDAY
    )
    val coroutineScope = rememberCoroutineScope()

    val canApply = mode == SelectionMode.SINGLE || rangeEnd != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = NovariColors.Background,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = NovariColors.Border)
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = 24.dp,
                    vertical = 16.dp
                )
        ) {

            Text(
                text = "Select date",
                style = MaterialTheme.typography.titleLarge,
                color = NovariColors.Navy
            )

            Spacer(modifier = Modifier.height(16.dp))

            SelectionModeToggle(
                mode = mode,
                onModeSelected = { newMode ->
                    if (newMode != mode) {
                        mode = newMode
                        rangeEnd = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MonthHeader(
                month = state.firstVisibleMonth,
                canGoPrevious = state.firstVisibleMonth.yearMonth > startMonth,
                canGoNext = state.firstVisibleMonth.yearMonth < currentMonth,
                onPrevious = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                    }
                },
                onNext = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                    }
                }
            )

            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    CalendarDayItem(
                        day = day,
                        today = today,
                        mode = mode,
                        rangeStart = rangeStart,
                        rangeEnd = rangeEnd,
                        onDateSelected = { date ->
                            when (mode) {
                                SelectionMode.SINGLE -> {
                                    rangeStart = date
                                }
                                SelectionMode.RANGE -> {
                                    if (rangeEnd != null || date.isBefore(rangeStart)) {
                                        rangeStart = date
                                        rangeEnd = null
                                    } else if (date.isAfter(rangeStart)) {
                                        rangeEnd = date
                                    } else {
                                        rangeStart = date
                                    }
                                }
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = {
                        rangeEnd = null
                        rangeStart = today
                        onClear()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovariColors.Navy
                    )
                }

                Button(
                    onClick = {
                        val filter = when (mode) {
                            SelectionMode.SINGLE -> DateFilter.Single(rangeStart)
                            SelectionMode.RANGE -> {
                                val end = rangeEnd
                                if (end != null) DateFilter.Range(rangeStart, end) else return@Button
                            }
                        }
                        onApply(filter)
                    },
                    enabled = canApply,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NovariColors.Teal
                    )
                ) {
                    Text(
                        text = "Apply",
                        style = MaterialTheme.typography.labelLarge,
                        color = NovariColors.Surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SelectionModeToggle(
    mode: SelectionMode,
    onModeSelected: (SelectionMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NovariColors.Surface)
            .padding(4.dp)
    ) {
        SelectionModeOption(
            text = "Single date",
            selected = mode == SelectionMode.SINGLE,
            onClick = { onModeSelected(SelectionMode.SINGLE) },
            modifier = Modifier.weight(1f)
        )
        SelectionModeOption(
            text = "Date range",
            selected = mode == SelectionMode.RANGE,
            onClick = { onModeSelected(SelectionMode.RANGE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectionModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NovariColors.Teal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) NovariColors.Surface else NovariColors.Slate
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun MonthHeader(
    month: CalendarMonth,
    canGoPrevious: Boolean,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = month.yearMonth.format(
                DateTimeFormatter.ofPattern("MMMM yyyy")
            ),
            style = MaterialTheme.typography.titleMedium,
            color = NovariColors.Navy
        )

        Row {

            IconButton(
                onClick = onPrevious,
                enabled = canGoPrevious
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowLeft,
                    contentDescription = "Previous month",
                    tint = if (canGoPrevious) NovariColors.Navy else NovariColors.Slate
                )
            }

            IconButton(
                onClick = onNext,
                enabled = canGoNext
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowRight,
                    contentDescription = "Next month",
                    tint = if (canGoNext) NovariColors.Navy else NovariColors.Slate
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarDayItem(
    day: CalendarDay,
    today: LocalDate,
    mode: SelectionMode,
    rangeStart: LocalDate,
    rangeEnd: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val date = day.date
    val isInMonth = day.position == DayPosition.MonthDate
    val isFuture = date.isAfter(today)
    val isEnabled = isInMonth && !isFuture

    val isStart = mode == SelectionMode.RANGE && date == rangeStart && rangeEnd != null
    val isEnd = mode == SelectionMode.RANGE && rangeEnd != null && date == rangeEnd
    val isSingleSelected = mode == SelectionMode.SINGLE && date == rangeStart
    val isRangeAnchor = mode == SelectionMode.RANGE && rangeEnd == null && date == rangeStart
    val isInRange = mode == SelectionMode.RANGE && rangeEnd != null &&
        date.isAfter(rangeStart) && date.isBefore(rangeEnd)

    val isSelected = isSingleSelected || isStart || isEnd || isRangeAnchor

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                color = if (isInRange) NovariColors.PaleTeal else Color.Transparent
            )
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) NovariColors.Teal else Color.Transparent
                )
                .then(
                    if (isEnabled) {
                        Modifier.clickable { onDateSelected(date) }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = date.dayOfMonth.toString(),
                color = when {
                    isSelected -> NovariColors.Surface
                    !isEnabled -> NovariColors.Slate.copy(alpha = 0.4f)
                    else -> NovariColors.Navy
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
