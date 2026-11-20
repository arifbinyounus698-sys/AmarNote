package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditLocationAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.domain.LocalizationHelper
import com.example.domain.QiblaData
import com.example.ui.components.IconBadge3D
import com.example.ui.components.LocationPickerDialog
import com.example.ui.components.NotebookPageCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    settings: AppSettings,
    qiblaData: QiblaData,
    onSelectLocation: (String, String, Double, Double) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var deviceAzimuthDeg by remember { mutableFloatStateOf(0f) }
    var manualAzimuthDeg by remember { mutableFloatStateOf(0f) }
    var sensorAvailable by remember { mutableStateOf(true) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showCalibrationHelp by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotVector = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnet = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sensorManager == null || (rotVector == null && (accel == null || magnet == null))) {
            sensorAvailable = false
            onDispose { }
        } else {
            sensorAvailable = true
            val grav = FloatArray(3)
            val geomag = FloatArray(3)
            var hasGrav = false
            var hasMag = false
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            var smoothedAngle = 0f
            var initialized = false

            fun smoothUpdate(newAzimuthDeg: Float) {
                if (!initialized) {
                    smoothedAngle = newAzimuthDeg
                    initialized = true
                } else {
                    var delta = newAzimuthDeg - smoothedAngle
                    while (delta > 180f) delta -= 360f
                    while (delta < -180f) delta += 360f
                    smoothedAngle = (smoothedAngle + 0.18f * delta + 360f) % 360f
                }
                deviceAzimuthDeg = smoothedAngle
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientation)
                        val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                        smoothUpdate(deg)
                    } else {
                        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            System.arraycopy(event.values, 0, grav, 0, 3)
                            hasGrav = true
                        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                            System.arraycopy(event.values, 0, geomag, 0, 3)
                            hasMag = true
                        }
                        if (hasGrav && hasMag && SensorManager.getRotationMatrix(rotMatrix, null, grav, geomag)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                            smoothUpdate(deg)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD || sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                        sensorAccuracy = accuracy
                    }
                }
            }

            if (rotVector != null) {
                sensorManager.registerListener(listener, rotVector, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(listener, magnet, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val activeAzimuth = if (sensorAvailable) deviceAzimuthDeg else manualAzimuthDeg
    val qiblaBearing = qiblaData.bearingDegrees.toFloat()

    // Difference between phone heading and Qibla bearing (-180..180)
    val diffToQibla = remember(activeAzimuth, qiblaBearing) {
        var d = qiblaBearing - activeAzimuth
        while (d > 180f) d -= 360f
        while (d < -180f) d += 360f
        d
    }
    val isAligned = abs(diffToQibla) <= 4.0f

    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned && sensorAvailable) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        wasAligned = isAligned
    }

    val animatedDialRotation by animateFloatAsState(
        targetValue = -activeAzimuth,
        animationSpec = spring(stiffness = 260f),
        label = "compass_dial"
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Location & Qibla Degree Header
            item {
                NotebookPageCard(accentColor = AccentTeal, showRuledLines = false) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settings.displayLocationName(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val degStr = LocalizationHelper.formatDecimal(qiblaData.bearingDegrees, useBnDigits, 1)
                            val distStr = LocalizationHelper.formatInt(qiblaData.distanceKm.toInt(), useBnDigits)
                            Text(
                                text = if (isBn) "কিবলার দিক: $degStr° • কাবা থেকে দূরত্ব: $distStr কিমি"
                                else "Qibla Bearing: $degStr° • Distance: $distStr km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = { showLocationDialog = true }) {
                            Icon(Icons.Filled.EditLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (isBn) "অবস্থান" else "Location")
                        }
                    }
                }
            }

            // Alignment Status Pill
            item {
                Surface(
                    color = if (isAligned) AccentEmerald.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isAligned) Icons.Filled.CheckCircle else Icons.Filled.Explore,
                            contentDescription = null,
                            tint = if (isAligned) AccentEmerald else MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isAligned) {
                                if (isBn) "মাশাআল্লাহ! সঠিক কিবলার দিকে মুখ করা আছে" else "Aligned with Qibla direction!"
                            } else {
                                val deg = LocalizationHelper.formatInt(abs(diffToQibla).toInt(), useBnDigits)
                                val turnDir = if (diffToQibla > 0) {
                                    if (isBn) "ডানদিকে" else "right"
                                } else {
                                    if (isBn) "বামদিকে" else "left"
                                }
                                if (isBn) "সঠিক কিবলার জন্য ফোন $deg° $turnDir ঘোরান"
                                else "Turn $deg° $turnDir to face Qibla"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAligned) AccentEmerald else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 360-Degree Interactive Compass Dial
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val primaryCol = MaterialTheme.colorScheme.primary
                    val ringCol = if (isAligned) AccentEmerald else MaterialTheme.colorScheme.outline
                    val surfaceCol = MaterialTheme.colorScheme.surface
                    val onSurfaceCol = MaterialTheme.colorScheme.onSurface

                    Canvas(modifier = Modifier.size(290.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 14.dp.toPx()

                        // Outer ring
                        drawCircle(
                            color = surfaceCol,
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = ringCol,
                            radius = radius,
                            center = center,
                            style = Stroke(width = if (isAligned) 6.dp.toPx() else 3.dp.toPx())
                        )

                        // Rotate compass rose by -azimuth
                        rotate(degrees = animatedDialRotation, pivot = center) {
                            // Degree ticks every 10 degrees
                            for (deg in 0 until 360 step 10) {
                                val rad = (deg - 90) * PI / 180.0
                                val isCardinal = deg % 90 == 0
                                val innerR = radius - if (isCardinal) 18.dp.toPx() else 10.dp.toPx()
                                val outerR = radius - 3.dp.toPx()
                                val p1 = Offset(
                                    center.x + (innerR * cos(rad)).toFloat(),
                                    center.y + (innerR * sin(rad)).toFloat()
                                )
                                val p2 = Offset(
                                    center.x + (outerR * cos(rad)).toFloat(),
                                    center.y + (outerR * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (deg == 0) AccentRose else onSurfaceCol.copy(alpha = if (isCardinal) 0.8f else 0.35f),
                                    start = p1,
                                    end = p2,
                                    strokeWidth = if (deg == 0) 4.dp.toPx() else if (isCardinal) 2.5.dp.toPx() else 1.2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // North Red Pointer (at 0 deg on dial)
                            val northPath = Path().apply {
                                moveTo(center.x, center.y - radius + 24.dp.toPx())
                                lineTo(center.x - 9.dp.toPx(), center.y - 24.dp.toPx())
                                lineTo(center.x + 9.dp.toPx(), center.y - 24.dp.toPx())
                                close()
                            }
                            drawPath(northPath, color = AccentRose)

                            // Distinct Qibla / Kaaba Pointer rotated to qiblaBearing on the dial
                            rotate(degrees = qiblaBearing, pivot = center) {
                                val qiblaPath = Path().apply {
                                    moveTo(center.x, center.y - radius + 16.dp.toPx())
                                    lineTo(center.x - 13.dp.toPx(), center.y - 12.dp.toPx())
                                    lineTo(center.x, center.y - 24.dp.toPx())
                                    lineTo(center.x + 13.dp.toPx(), center.y - 12.dp.toPx())
                                    close()
                                }
                                drawPath(qiblaPath, color = AccentEmerald)
                                drawLine(
                                    color = AccentEmerald,
                                    start = center,
                                    end = Offset(center.x, center.y - radius + 28.dp.toPx()),
                                    strokeWidth = 4.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Kaaba golden cube marker near tip
                                drawCircle(
                                    color = AccentAmber,
                                    radius = 9.dp.toPx(),
                                    center = Offset(center.x, center.y - radius + 34.dp.toPx())
                                )
                            }
                        }

                        // Fixed phone top reference marker
                        drawLine(
                            color = primaryCol,
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, 14.dp.toPx()),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Center pivot
                        drawCircle(color = primaryCol, radius = 8.dp.toPx(), center = center)
                    }
                }
            }

            // Legend & Degree Readouts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = AccentEmerald.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBn) "সবুজ/সোনালি তীর = কিবলা" else "Green/Gold = Qibla",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                            Text(
                                text = "${LocalizationHelper.formatDecimal(qiblaData.bearingDegrees, useBnDigits, 1)}°",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        color = AccentRose.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isBn) "লাল তীর = উত্তর (N)" else "Red Arrow = North (N)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentRose
                            )
                            Text(
                                text = "${LocalizationHelper.formatInt(activeAzimuth.toInt(), useBnDigits)}°",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMonoFamily),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Sensor fallback or Calibration guidance
            if (!sensorAvailable) {
                item {
                    NotebookPageCard(accentColor = AccentAmber, showRuledLines = false) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconBadge3D(icon = Icons.Filled.SensorsOff, tint = AccentAmber, contentDescription = null, size = 40.dp)
                                Text(
                                    text = if (isBn) "কম্পাস সেন্সর পাওয়া যায়নি — ম্যানুয়াল নির্দেশনা" else "No Compass Sensor — Manual Guide",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            val deg = LocalizationHelper.formatDecimal(qiblaData.bearingDegrees, useBnDigits, 1)
                            Text(
                                text = if (isBn) "এই ডিভাইসে ম্যাগনেটোমিটার সেন্সর নেই। ম্যানুয়ালভাবে উত্তর দিক থেকে ঘড়ির কাঁটার দিকে $deg° কোণে মুখ করুন (বাংলাদেশের ক্ষেত্রে প্রায় পশ্চিম-উত্তর-পশ্চিম দিক)।"
                                else "This device lacks a magnetometer sensor. Face $deg° clockwise from True North.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = if (isBn) "প্রিভিউ দেখতে স্লাইডার ঘোরান:" else "Rotate slider to preview heading:",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Slider(
                                value = manualAzimuthDeg,
                                onValueChange = { manualAzimuthDeg = it },
                                valueRange = 0f..360f
                            )
                        }
                    }
                }
            } else {
                item {
                    val accText = when (sensorAccuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> if (isBn) "উচ্চ নির্ভুলতা (High)" else "High Accuracy"
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> if (isBn) "মাঝারি নির্ভুলতা (Medium)" else "Medium Accuracy"
                        else -> if (isBn) "কম নির্ভুলতা — ক্যালিব্রেট করুন" else "Low — Calibrate Compass"
                    }
                    NotebookPageCard(accentColor = MaterialTheme.colorScheme.primary, showRuledLines = false) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "সেন্সর অবস্থা: $accText" else "Sensor: $accText",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Button(onClick = { showCalibrationHelp = !showCalibrationHelp }) {
                                    Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBn) "ক্যালিব্রেশন" else "Calibrate")
                                }
                            }
                            if (showCalibrationHelp || sensorAccuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
                                Text(
                                    text = if (isBn) "কিবলা কম্পাস সঠিকভাবে ক্যালিব্রেট করতে ফোনটি হাতে ধরে বাতাসে '৮' (Figure-8) আকৃতিতে ৩-৪ বার ঘোরান এবং চুম্বক বা ধাতব বস্তু থেকে দূরে রাখুন।"
                                    else "Move your phone in a Figure-8 (∞) motion 3–4 times away from magnets or metal objects to calibrate the compass.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationDialog) {
        LocationPickerDialog(
            settings = settings,
            onDismiss = { showLocationDialog = false },
            onSelectLocation = onSelectLocation,
            onMessage = onMessage
        )
    }
}
