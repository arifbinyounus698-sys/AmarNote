package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.local.AppSettings
import com.example.data.local.BackupManager
import com.example.data.local.ProfessionOption
import com.example.data.local.ProfileMediaManager
import com.example.data.local.UserProfile
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.components.PhotoCropDialog
import com.example.ui.components.PhotoPreviewDialog
import com.example.ui.components.ProfileAvatarView
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentIndigo
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRose
import com.example.ui.theme.AccentSkyBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    profile: UserProfile,
    settings: AppSettings,
    onSaveProfile: (UserProfile) -> Unit,
    onUpdateAvatarPath: (String) -> Unit,
    onUpdateCoverPath: (String) -> Unit,
    onDeleteAvatar: () -> Unit,
    onDeleteCover: () -> Unit,
    onResetProfile: () -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isBn = settings.isBangla

    var showEditDialog by remember { mutableStateOf(false) }
    var editFocusSection by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Photo preview / crop state
    var previewTarget by remember { mutableStateOf<Boolean?>(null) } // true=avatar, false=cover
    var cropTargetIsAvatar by remember { mutableStateOf(true) }
    var pendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraIsAvatar by remember { mutableStateOf(true) }

    val coverBitmap = remember(profile.coverPhotoPath) {
        ProfileMediaManager.loadLocalBitmap(profile.coverPhotoPath)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    ProfileMediaManager.loadBitmapFromUri(context, uri)
                }
                if (bmp != null) {
                    pendingCropBitmap = bmp
                } else {
                    onMessage(if (isBn) "ছবি লোড করা যায়নি" else "Failed to load image")
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) {
                    ProfileMediaManager.loadBitmapFromUri(context, uri)
                }
                if (bmp != null) {
                    cropTargetIsAvatar = pendingCameraIsAvatar
                    pendingCropBitmap = bmp
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ProfileMediaManager.createTempCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            onMessage(
                if (isBn) "ক্যামেরা অনুমতি প্রয়োজন; চাইলে গ্যালারি থেকে ছবি নির্বাচন করতে পারেন।"
                else "Camera permission denied; you can select a photo from gallery."
            )
        }
    }

    val exportPersonalLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = BackupManager.buildPersonalProfileExportJson(profile)
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    onMessage(if (isBn) "ব্যক্তিগত প্রোফাইল এক্সপোর্ট সম্পন্ন হয়েছে" else "Personal profile exported")
                } catch (e: Exception) {
                    onMessage(e.localizedMessage ?: "Export error")
                }
            }
        }
    }

    fun launchGallery(isAvatar: Boolean) {
        cropTargetIsAvatar = isAvatar
        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun launchCamera(isAvatar: Boolean) {
        pendingCameraIsAvatar = isAvatar
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            val uri = ProfileMediaManager.createTempCameraUri(context)
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cover Photo + Overlapping Circular Profile Photo Header
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                                .clickable { previewTarget = false }
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0F766E), Color(0xFF6D28D9))
                                    )
                                )
                        ) {
                            if (coverBitmap != null) {
                                Image(
                                    bitmap = coverBitmap.asImageBitmap(),
                                    contentDescription = if (isBn) "কভার ফটো" else "Cover Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Surface(
                                color = Color.Black.copy(alpha = 0.48f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .clickable { previewTarget = false }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = if (isBn) "কভার ফটো" else "Cover Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(50.dp))
                    }

                    // Circular Profile Photo + Camera badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-6).dp)
                    ) {
                        ProfileAvatarView(
                            photoPath = profile.profilePhotoPath,
                            fullName = profile.fullName,
                            size = 104.dp,
                            borderColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clickable { previewTarget = true }
                                .testTag("profile_avatar_view")
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                                .clickable { previewTarget = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.CameraAlt,
                                    contentDescription = if (isBn) "ছবি পরিবর্তন" else "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Name, Profession, About Me & Edit Button
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = profile.fullName.ifBlank {
                            if (isBn) "আপনার নাম যোগ করুন" else "Add Your Full Name"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (profile.profession.isNotBlank() || profile.designation.isNotBlank()) {
                        val roleLine = listOf(profile.profession, profile.designation)
                            .filter { it.isNotBlank() }
                            .joinToString(" • ")
                        Text(
                            text = roleLine,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (profile.aboutMe.isNotBlank()) {
                        Text(
                            text = profile.aboutMe,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        color = AccentEmerald.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (isBn) "ব্যক্তিগত তথ্য ডিফল্টভাবে Private ও অফলাইনে সংরক্ষিত" else "Private by default • Stored offline only",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentEmerald
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = {
                            editFocusSection = null
                            showEditDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_edit_button")
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isBn) "প্রোফাইল সম্পাদনা করুন" else "Edit Profile")
                    }
                }
            }

            // Personal Details Card (14 fields summary)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NotebookPageCard(accentColor = AccentPurple) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = if (isBn) "ব্যক্তিগত ও কর্মস্থলের তথ্য" else "Personal & Work Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                            ProfileInfoRow(Icons.Filled.Phone, if (isBn) "মোবাইল নম্বর" else "Mobile Number", profile.mobileNumber, isBn)
                            ProfileInfoRow(Icons.Filled.Phone, if (isBn) "বিকল্প মোবাইল" else "Alt. Mobile", profile.altMobileNumber, isBn)
                            ProfileInfoRow(Icons.Filled.Email, if (isBn) "ইমেইল ঠিকানা" else "Email", profile.email, isBn)
                            ProfileInfoRow(Icons.Filled.Work, if (isBn) "পেশা" else "Profession", profile.profession, isBn)
                            ProfileInfoRow(Icons.Filled.Business, if (isBn) "প্রতিষ্ঠানের নাম" else "Organization", profile.organizationName, isBn)
                            ProfileInfoRow(Icons.Filled.Badge, if (isBn) "পদবি" else "Designation", profile.designation, isBn)
                            ProfileInfoRow(Icons.Filled.LocationOn, if (isBn) "কর্মস্থলের ঠিকানা" else "Workplace Address", profile.workplaceAddress, isBn)
                            ProfileInfoRow(Icons.Filled.LocationCity, if (isBn) "বর্তমান শহর ও দেশ" else "City & Country", profile.cityAndCountry, isBn)
                            ProfileInfoRow(Icons.Filled.Cake, if (isBn) "জন্মতারিখ (ঐচ্ছিক)" else "Date of Birth", profile.dateOfBirth, isBn)
                            if (profile.extraComment.isNotBlank()) {
                                ProfileInfoRow(Icons.Filled.Person, if (isBn) "অতিরিক্ত মন্তব্য" else "Extra Comment", profile.extraComment, isBn)
                            }
                        }
                    }
                }
            }

            // Profile Settings & Quick Actions (Requirement 6)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    NotebookPageCard(accentColor = AccentSkyBlue, showRuledLines = false) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isBn) "প্রোফাইল সেটিংস ও নিয়ন্ত্রণ" else "Profile Settings & Actions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentSkyBlue
                            )
                            ProfileSettingActionRow(if (isBn) "নাম পরিবর্তন" else "Change Name") {
                                editFocusSection = "name"; showEditDialog = true
                            }
                            ProfileSettingActionRow(if (isBn) "প্রোফাইল ছবি পরিবর্তন / Preview" else "Change Profile Picture") {
                                previewTarget = true
                            }
                            ProfileSettingActionRow(if (isBn) "কভার ফটো পরিবর্তন / Preview" else "Change Cover Photo") {
                                previewTarget = false
                            }
                            ProfileSettingActionRow(if (isBn) "মোবাইল নম্বর পরিবর্তন" else "Change Mobile Number") {
                                editFocusSection = "phone"; showEditDialog = true
                            }
                            ProfileSettingActionRow(if (isBn) "ইমেইল পরিবর্তন" else "Change Email") {
                                editFocusSection = "email"; showEditDialog = true
                            }
                            ProfileSettingActionRow(if (isBn) "পেশা আপডেট" else "Update Profession") {
                                editFocusSection = "profession"; showEditDialog = true
                            }
                            ProfileSettingActionRow(if (isBn) "নিজের পরিচয় (About Me) আপডেট" else "Update About Me") {
                                editFocusSection = "about"; showEditDialog = true
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { exportPersonalLauncher.launch("AmarNote_Personal_Profile.json") },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBn) "তথ্য এক্সপোর্ট" else "Export Info")
                                }
                                OutlinedButton(
                                    onClick = { showResetConfirm = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRose),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isBn) "রিসেট প্রোফাইল" else "Reset Profile")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Photo Preview Dialog
    previewTarget?.let { isAvatar ->
        PhotoPreviewDialog(
            photoPath = if (isAvatar) profile.profilePhotoPath else profile.coverPhotoPath,
            title = if (isAvatar) (if (isBn) "প্রোফাইল ছবি" else "Profile Photo") else (if (isBn) "কভার ফটো" else "Cover Photo"),
            isBangla = isBn,
            onDismiss = { previewTarget = null },
            onPickGallery = { launchGallery(isAvatar) },
            onTakeCamera = { launchCamera(isAvatar) },
            onDeletePhoto = {
                if (isAvatar) onDeleteAvatar() else onDeleteCover()
            }
        )
    }

    // Photo Crop & Resize Dialog
    pendingCropBitmap?.let { bmp ->
        PhotoCropDialog(
            initialBitmap = bmp,
            isAvatar = cropTargetIsAvatar,
            isBangla = isBn,
            onDismiss = { pendingCropBitmap = null },
            onConfirm = { workingBmp, zoom, ox, oy ->
                scope.launch {
                    val savedPath = withContext(Dispatchers.IO) {
                        ProfileMediaManager.cropAndSavePhoto(
                            context = context,
                            source = workingBmp,
                            isAvatar = cropTargetIsAvatar,
                            zoomScale = zoom,
                            offsetXFraction = ox,
                            offsetYFraction = oy
                        )
                    }
                    if (cropTargetIsAvatar) {
                        onUpdateAvatarPath(savedPath)
                    } else {
                        onUpdateCoverPath(savedPath)
                    }
                    pendingCropBitmap = null
                }
            }
        )
    }

    // Full Profile Edit Dialog (All 14 fields + Profession selector)
    if (showEditDialog) {
        ProfileEditorDialog(
            initial = profile,
            isBangla = isBn,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                onSaveProfile(updated)
                showEditDialog = false
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(if (isBn) "প্রোফাইলের তথ্য রিসেট করবেন?" else "Reset Profile Information?") },
            text = {
                Text(
                    if (isBn) "এতে আপনার ব্যক্তিগত প্রোফাইলের তথ্য ও ছবি মুছে যাবে, তবে বেতনের কোনো হিসাব মুছবে না।"
                    else "This clears your personal profile details and photos, while keeping all salary records intact."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetProfile()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "রিসেট করুন" else "Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    isBangla: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconBadge3D(icon = icon, tint = MaterialTheme.colorScheme.primary, contentDescription = null, size = 34.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value.ifBlank { if (isBangla) "যুক্ত করা হয়নি" else "Not set" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                color = if (value.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileSettingActionRow(title: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditorDialog(
    initial: UserProfile,
    isBangla: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var fullName by remember { mutableStateOf(initial.fullName) }
    var mobileNumber by remember { mutableStateOf(initial.mobileNumber) }
    var altMobileNumber by remember { mutableStateOf(initial.altMobileNumber) }
    var email by remember { mutableStateOf(initial.email) }
    var aboutMe by remember { mutableStateOf(initial.aboutMe) }
    var profession by remember { mutableStateOf(initial.profession) }
    var organizationName by remember { mutableStateOf(initial.organizationName) }
    var designation by remember { mutableStateOf(initial.designation) }
    var workplaceAddress by remember { mutableStateOf(initial.workplaceAddress) }
    var cityAndCountry by remember { mutableStateOf(initial.cityAndCountry) }
    var dateOfBirth by remember { mutableStateOf(initial.dateOfBirth) }
    var extraComment by remember { mutableStateOf(initial.extraComment) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBangla) "ব্যক্তিগত প্রোফাইল সম্পাদনা" else "Edit Personal Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(if (isBangla) "পূর্ণ নাম" else "Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_fullname_input")
                )

                Text(
                    text = if (isBangla) "পেশা নির্বাচন করুন অথবা নিচে লিখুন:" else "Select or enter your profession:",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProfessionOption.entries.forEach { option ->
                        val label = option.label(isBangla)
                        FilterChip(
                            selected = profession == label,
                            onClick = {
                                profession = if (option == ProfessionOption.OTHER) "" else label
                            },
                            label = { Text(label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = profession,
                    onValueChange = { profession = it },
                    label = { Text(if (isBangla) "পেশা (নিজেও লিখতে পারেন)" else "Profession (Custom or Selected)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text(if (isBangla) "মোবাইল নম্বর (ঐচ্ছিক)" else "Mobile Number (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = altMobileNumber,
                    onValueChange = { altMobileNumber = it },
                    label = { Text(if (isBangla) "বিকল্প মোবাইল নম্বর (ঐচ্ছিক)" else "Alternative Mobile (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(if (isBangla) "ইমেইল ঠিকানা (ঐচ্ছিক)" else "Email Address (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = aboutMe,
                    onValueChange = { aboutMe = it },
                    label = { Text(if (isBangla) "নিজের পরিচয় (About Me)" else "About Me") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = organizationName,
                    onValueChange = { organizationName = it },
                    label = { Text(if (isBangla) "প্রতিষ্ঠানের নাম" else "Organization Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text(if (isBangla) "পদবি" else "Designation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = workplaceAddress,
                    onValueChange = { workplaceAddress = it },
                    label = { Text(if (isBangla) "কর্মস্থলের ঠিকানা (ঐচ্ছিক)" else "Workplace Address (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cityAndCountry,
                    onValueChange = { cityAndCountry = it },
                    label = { Text(if (isBangla) "বর্তমান শহর ও দেশ" else "Current City & Country") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateOfBirth,
                    onValueChange = { dateOfBirth = it },
                    label = { Text(if (isBangla) "জন্মতারিখ (ঐচ্ছিক, যেমন: ১৫ মে ১৯৯৫)" else "Date of Birth (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = extraComment,
                    onValueChange = { extraComment = it },
                    label = { Text(if (isBangla) "নিজের সম্পর্কে অতিরিক্ত মন্তব্য" else "Extra Comment") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBangla) "বাতিল" else "Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                initial.copy(
                                    fullName = fullName.trim(),
                                    mobileNumber = mobileNumber.trim(),
                                    altMobileNumber = altMobileNumber.trim(),
                                    email = email.trim(),
                                    aboutMe = aboutMe.trim(),
                                    profession = profession.trim(),
                                    organizationName = organizationName.trim(),
                                    designation = designation.trim(),
                                    workplaceAddress = workplaceAddress.trim(),
                                    cityAndCountry = cityAndCountry.trim(),
                                    dateOfBirth = dateOfBirth.trim(),
                                    extraComment = extraComment.trim()
                                )
                            )
                        },
                        modifier = Modifier.testTag("profile_save_button")
                    ) {
                        Text(if (isBangla) "সংরক্ষণ করুন" else "Save Profile")
                    }
                }
            }
        }
    }
}
