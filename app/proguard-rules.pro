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

# ========== Media3 ProGuard Rules ==========

# Keep Media3 ExoPlayer classes
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.session.** { *; }

# Keep Media3 data source classes
-keep class androidx.media3.datasource.** { *; }
-keep class androidx.media3.extractor.** { *; }

# Keep Media3 decoder classes
-keep class androidx.media3.decoder.** { *; }

# Keep Media3 transformer classes (if using)
-keep class androidx.media3.transformer.** { *; }

# Keep Media3 cast extension (if using)
-keep class androidx.media3.cast.** { *; }

# Keep Media3 effect classes (if using)
-keep class androidx.media3.effect.** { *; }

# Preserve Media3 annotations
-keepattributes *Annotation*

# Keep Media3 native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Media3 serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep Media3 Parcelable classes
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# Keep Media3 MediaSession related classes
-keep class androidx.media3.session.MediaSession { *; }
-keep class androidx.media3.session.MediaController { *; }
-keep class androidx.media3.session.MediaLibraryService { *; }
-keep class androidx.media3.session.MediaBrowser { *; }

# Keep custom Media3 service classes (adjust package name as needed)
-keep class com.wzh.common.media.** { *; }

# Prevent obfuscation of Media3 callback methods
-keepclassmembers class * {
    @androidx.media3.common.util.UnstableApi <methods>;
}

# Keep Media3 format classes
-keep class androidx.media3.common.Format { *; }
-keep class androidx.media3.common.MediaItem { *; }
-keep class androidx.media3.common.MediaMetadata { *; }

# ========== End Media3 ProGuard Rules ==========