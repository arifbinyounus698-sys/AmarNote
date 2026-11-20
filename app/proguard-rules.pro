# Preserve line numbers for readable stack traces while keeping R8 optimization enabled
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Manifest-declared components (Activity, BroadcastReceiver)
-keep public class com.example.MainActivity
-keep public class com.example.data.local.ReminderReceiver

# Keep Room Entities, DAOs, and Database implementations
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.data.local.SalaryRecord { *; }
-keep class com.example.data.local.CalculatorHistoryItem { *; }
-keep class com.example.data.local.UserProfile { *; }
-keep class com.example.data.local.PersonalNote { *; }
-keep class com.example.data.local.AppReminder { *; }

# Keep BiometricPrompt callbacks and crypto/security classes
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**
