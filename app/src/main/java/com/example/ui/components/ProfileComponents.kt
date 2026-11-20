package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.data.local.AppSettings
import com.example.data.local.ProfileMediaManager
import com.example.data.local.SecurityManager
import com.example.data.local.UserProfile
import com.example.domain.LocalizationHelper
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose

@Composable
fun ProfileAvatarView(
    photoPath: String,
    fullName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.primary
) {
    val bitmap = remember(photoPath) { ProfileMediaManager.loadLocalBitmap(photoPath) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = fullName.ifBlank { "Profile Photo" },
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = fullName.trim().firstOrNull()?.uppercaseChar()?.toString()
            if (initial != null && size >= 40.dp) {
                Text(
                    text = initial,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.42f).sp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Default Profile Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.56f)
                )
            }
        }
    }
}

@Composable
fun ProfileQuickSheetDialog(
    profile: UserProfile,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenAccountSettings: () -> Unit
) {
    val isBn = settings.isBangla
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = AccentEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isBn) "প্রাইভেট ও অফলাইন" else "Private & Offline",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = if (isBn) "বন্ধ" else "Close")
                    }
                }

                ProfileAvatarView(
                    photoPath = profile.profilePhotoPath,
                    fullName = profile.fullName,
                    size = 86.dp
                )

                Text(
                    text = profile.fullName.ifBlank {
                        if (isBn) "নাম যোগ করা হয়নি" else "Your Name"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (profile.profession.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = profile.profession,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (settings.showPhoneInProfileCard && profile.mobileNumber.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = profile.mobileNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (profile.aboutMe.isNotBlank()) {
                    Text(
                        text = profile.aboutMe,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                HorizontalDivider()

                Button(
                    onClick = {
                        onDismiss()
                        onEditProfile()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_edit_profile_button")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBn) "প্রোফাইল দেখুন ও সম্পাদনা করুন" else "View & Edit Profile")
                }

                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenAccountSettings()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBn) "অ্যাকাউন্ট ও নিরাপত্তা সেটিংস" else "Account & Security Settings")
                }
            }
        }
    }
}

@Composable
fun PhotoCropDialog(
    initialBitmap: Bitmap,
    isAvatar: Boolean,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (croppedBitmap: Bitmap, zoom: Float, offsetXFrac: Float, offsetYFrac: Float) -> Unit
) {
    var workingBitmap by remember(initialBitmap) { mutableStateOf(initialBitmap) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var offsetXFrac by remember { mutableFloatStateOf(0f) }
    var offsetYFrac by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ছবি Crop ও Resize করুন" else "Crop & Resize Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        workingBitmap = ProfileMediaManager.rotateBitmap90(workingBitmap)
                    }) {
                        Icon(Icons.Filled.RotateRight, contentDescription = if (isBangla) "ঘোরান" else "Rotate")
                    }
                }

                // Crop Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (isAvatar) 1f else 16f / 9f)
                        .clip(if (isAvatar) CircleShape else RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(2.dp, MaterialTheme.colorScheme.primary, if (isAvatar) CircleShape else RoundedCornerShape(16.dp))
                        .pointerInput(zoomScale) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offsetXFrac = (offsetXFrac + dragAmount.x / 200f).coerceIn(-1f, 1f)
                                offsetYFrac = (offsetYFrac + dragAmount.y / 200f).coerceIn(-1f, 1f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = workingBitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = offsetXFrac * 120f
                                translationY = offsetYFrac * 120f
                            }
                    )
                }

                Text(
                    text = if (isBangla) "ছবিটি টেনে পজিশন ঠিক করুন এবং স্লাইডার দিয়ে জুম করুন" else "Drag photo to position and use slider to zoom",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isBangla) "জুম" else "Zoom", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(8.dp))
                    Slider(
                        value = zoomScale,
                        onValueChange = { zoomScale = it },
                        valueRange = 1f..3.5f,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(workingBitmap, zoomScale, offsetXFrac, offsetYFrac) }
                    ) {
                        Text(if (isBangla) "সংরক্ষণ" else "Apply & Save")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPreviewDialog(
    photoPath: String,
    title: String,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onPickGallery: () -> Unit,
    onTakeCamera: () -> Unit,
    onDeletePhoto: () -> Unit
) {
    val bmp = remember(photoPath) { ProfileMediaManager.loadLocalBitmap(photoPath) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(84.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onPickGallery()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBangla) "গ্যালারি" else "Gallery")
                    }
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onTakeCamera()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBangla) "ক্যামেরা" else "Camera")
                    }
                }

                if (bmp != null) {
                    TextButton(
                        onClick = {
                            onDeletePhoto()
                            onDismiss()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBangla) "ছবি মুছে ফেলুন" else "Delete Photo")
                    }
                }
            }
        }
    }
}

@Composable
fun LockScreenOverlay(
    settings: AppSettings,
    onVerifyPin: (String) -> Boolean,
    onBiometricUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits
    var enteredPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val biometricAvailable = remember(context, settings.biometricEnabled) {
        settings.biometricEnabled && SecurityManager.isBiometricAvailable(context)
    }

    fun triggerBiometric() {
        val activity = context as? FragmentActivity ?: return
        SecurityManager.showBiometricPrompt(
            activity = activity,
            title = if (isBn) "AmarNote আনলক করুন" else "Unlock AmarNote",
            subtitle = if (isBn) "ফিঙ্গারপ্রিন্ট বা বায়োমেট্রিক ব্যবহার করুন" else "Use fingerprint or biometric sensor",
            negativeText = if (isBn) "PIN ব্যবহার করুন" else "Use PIN",
            onSuccess = onBiometricUnlocked,
            onError = { /* User stays on PIN keypad */ }
        )
    }

    LaunchedEffect(biometricAvailable) {
        if (biometricAvailable) {
            triggerBiometric()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconBadge3D(
                icon = Icons.Filled.Lock,
                tint = AccentPurple,
                contentDescription = null,
                size = 64.dp
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = if (isBn) "AmarNote লক করা আছে" else "AmarNote is Locked",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isBn) "প্রবেশ করতে আপনার PIN লিখুন" else "Enter your PIN to unlock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))

            // PIN Dots
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(4) { idx ->
                    val filled = idx < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                if (filled) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                1.5.dp,
                                if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                CircleShape
                            )
                    )
                }
            }

            errorMsg?.let { msg ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = msg,
                    color = AccentRose,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(22.dp))

            val keypadRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(if (biometricAvailable) "BIO" else "C", "0", "DEL")
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.width(264.dp)
            ) {
                keypadRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            Button(
                                onClick = {
                                    errorMsg = null
                                    when (key) {
                                        "DEL" -> if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                                        "C" -> enteredPin = ""
                                        "BIO" -> triggerBiometric()
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                if (enteredPin.length == 4) {
                                                    val ok = onVerifyPin(enteredPin)
                                                    if (!ok) {
                                                        errorMsg = if (isBn) "ভুল PIN! আবার চেষ্টা করুন" else "Wrong PIN! Try again"
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(62.dp)
                                    .testTag("lock_key_$key")
                            ) {
                                when (key) {
                                    "DEL" -> Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                                    "BIO" -> Icon(Icons.Filled.Fingerprint, contentDescription = "Fingerprint", tint = MaterialTheme.colorScheme.primary)
                                    else -> Text(
                                        text = if (key in "0".."9") LocalizationHelper.toLocalizedDigits(key, useBnDigits) else key,
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
    }
}
