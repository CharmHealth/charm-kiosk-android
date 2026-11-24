# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Missing classes - R8 warnings suppression
# These classes are optional dependencies from third-party libraries that are not used in this project
# Generated from missing_rules.txt


-dontobfuscate
-dontoptimize
-verbose
-dontshrink


-keepattributes SourceFile,LineNumberTable

# Zoho Apptics Feedback - Optional module
-dontwarn com.zoho.apptics.feedback.AppticsFeedback

# Zoho Quartz UI Theme - Optional dependency for Apptics screen recording
-dontwarn com.zoho.quartz.ui.theme.ColorScheme
-dontwarn com.zoho.quartz.ui.theme.QuartzTheme
-dontwarn com.zoho.quartz.ui.theme.UiMode

# Timber Logging - Optional dependency
-dontwarn timber.log.Timber$Tree

# Keep navigation argument classes - CRITICAL for type-safe navigation
# Prevent R8 from obfuscating classes used as navigation arguments
-keep class com.charm.ehr.kiosk.ui.navigation.** { *; }
-keep class com.charm.ehr.kiosk.util.SecurityUtil$AuthType { *; }

# Keep all @Serializable classes and their members for Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all serializable classes
-keep,includedescriptorclasses class com.charm.ehr.kiosk.**$$serializer { *; }
-keepclassmembers class com.charm.ehr.kiosk.** {
    *** Companion;
}
-keepclasseswithmembers class com.charm.ehr.kiosk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep enums - they're often used in navigation and serialization
-keepclassmembers enum com.charm.ehr.kiosk.** { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep data classes used in navigation
-keep @kotlinx.serialization.Serializable class * { *; }

## Remove all Log.v, Log.d, Log.i calls
#-assumenosideeffects class android.util.Log {
#    public static *** v(...);
#    public static *** d(...);
#    public static *** i(...);
#}

