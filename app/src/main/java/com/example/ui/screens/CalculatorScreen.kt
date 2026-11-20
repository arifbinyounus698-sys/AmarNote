package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppSettings
import com.example.data.local.CalculatorHistoryItem
import com.example.domain.LocalizationHelper
import com.example.ui.components.NotebookPageCard
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentSoftPink
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.viewmodel.CalculatorUiState

@Composable
fun CalculatorScreen(
    state: CalculatorUiState,
    history: List<CalculatorHistoryItem>,
    settings: AppSettings,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPercent: () -> Unit,
    onToggleSign: () -> Unit,
    onMemoryAction: (String) -> Unit,
    onUseHistoryResult: (String) -> Unit,
    onClearHistory: () -> Unit,
    onAddResultToSalary: (Double) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    val displayLocalized = LocalizationHelper.toLocalizedDigits(state.display, useBnDigits)
    val expressionLocalized = LocalizationHelper.toLocalizedDigits(state.expression, useBnDigits)
    val hasMemory = settings.calculatorMemory != 0.0

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Display Screen
            item {
                NotebookPageCard(accentColor = AccentSoftPink, showRuledLines = false) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (hasMemory) AccentPurple.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (hasMemory) {
                                        "M = ${LocalizationHelper.toLocalizedDigits(settings.calculatorMemory.toString(), useBnDigits)}"
                                    } else {
                                        if (isBn) "মেমোরি খালি" else "M Empty"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (hasMemory) AccentPurple else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = expressionLocalized,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = displayLocalized,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 38.sp
                            ),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("calculator_display")
                        )

                        val numericValue = state.display.toDoubleOrNull()
                        if (numericValue != null && numericValue > 0.0) {
                            OutlinedButton(
                                onClick = { onAddResultToSalary(numericValue) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(if (isBn) "বেতন হিসাবে যোগ করুন" else "Use as Salary Amount")
                            }
                        }
                    }
                }
            }

            // Memory Row: MC, MR, M+, M-, MS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("MC", "MR", "M+", "M-", "MS").forEach { memKey ->
                        FilledTonalButton(
                            onClick = { onMemoryAction(memKey) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text(memKey, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Keypad Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val rows = listOf(
                        listOf("C", "⌫", "%", "÷"),
                        listOf("7", "8", "9", "×"),
                        listOf("4", "5", "6", "-"),
                        listOf("1", "2", "3", "+"),
                        listOf("±", "0", ".", "=")
                    )
                    rows.forEach { rowKeys ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowKeys.forEach { key ->
                                val isOp = key in listOf("÷", "×", "-", "+")
                                val isEq = key == "="
                                val isClear = key == "C" || key == "⌫"
                                val containerColor = when {
                                    isEq -> MaterialTheme.colorScheme.primary
                                    isOp -> AccentOrange.copy(alpha = 0.18f)
                                    isClear -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                                val contentColor = when {
                                    isEq -> MaterialTheme.colorScheme.onPrimary
                                    isOp -> AccentOrange
                                    isClear -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                                val labelText = if (key in "0".."9") {
                                    LocalizationHelper.toLocalizedDigits(key, useBnDigits)
                                } else {
                                    key
                                }

                                Button(
                                    onClick = {
                                        when (key) {
                                            "C" -> onClear()
                                            "⌫" -> onBackspace()
                                            "%" -> onPercent()
                                            "±" -> onToggleSign()
                                            "=" -> onEquals()
                                            "÷", "×", "-", "+" -> onOperator(key)
                                            else -> onDigit(key)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = containerColor,
                                        contentColor = contentColor
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(58.dp)
                                        .testTag("calc_key_$key")
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = if (isBn) "মুছুন" else "Delete"
                                        )
                                    } else {
                                        Text(
                                            text = labelText,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculation History
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = if (isBn) "হিসাবের ইতিহাস" else "Calculation History",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (history.isNotEmpty()) {
                        IconButton(onClick = onClearHistory) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = if (isBn) "ইতিহাস মুছুন" else "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (history.isEmpty()) {
                item {
                    Text(
                        text = if (isBn) "এখনো কোনো হিসাব সংরক্ষিত হয়নি।" else "No calculation history yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(history, key = { it.id }) { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onUseHistoryResult(item.result) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LocalizationHelper.toLocalizedDigits(item.expression, useBnDigits),
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFamily),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "= ${LocalizationHelper.toLocalizedDigits(item.result, useBnDigits)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider(color = Color.Transparent, modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
