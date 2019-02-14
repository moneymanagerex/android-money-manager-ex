# Taken from https://gist.github.com/Jackgris/c4a71328b1ae346cba04
# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
#-dontpreverify

# If you want to enable optimization, you should include the
# following:
#-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
#-optimizationpasses 5
-allowaccessmodification

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v4.app.DialogFragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class * extends android.view.View {
 public <init>(android.content.Context);
 public <init>(android.content.Context, android.util.AttributeSet);
 public <init>(android.content.Context, android.util.AttributeSet, int);
 public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

# MMEX classes
#-keep class com.money.manager.ex.**
#-keep class com.money.manager.ex.home.RecentDatabasesProvider { *; }
#-keep class com.money.manager.ex.home.**
-keepclassmembers class com.money.manager.ex.home.DatabaseMetadata { <fields>; }
-keepclassmembers class com.money.manager.ex.datalayer.StockFields { public *; }

#Icon font
-keep class .R
-keep class **.R$* {
    <fields>;
}

# Parceler library
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keep class org.parceler.Parceler$$Parcels

-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

-keep class com.github.mikephil.charting.** { *; }
-dontwarn io.realm.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# EventBus
#-keepclassmembers class ** {
#    public void onEvent*(***);
#}
# EventBus 3.0, http://greenrobot.org/eventbus/documentation/proguard/
#-keep class de.greenrobot.event.** { *; }
#-keep class * {
#    @de.greenrobot.event.* <methods>;
#}
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# IcePick
-dontwarn icepick.**
-keep class icepick.** { *; }
-keep class **$$Icepick { *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}
-keepnames class * { @icepick.State *;}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
#-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
##---------------End: proguard configuration for Gson  ----------

#SQLCipher
#-keep class net.sqlcipher.** { *; }

# Ignore warnings
-dontwarn org.apache.**
-dontwarn com.opencsv.bean.**
-dontwarn com.google.common.**
-dontwarn sun.misc.Unsafe
# https://github.com/square/okio/issues/60
-dontwarn okio.**
# picasso
-dontwarn com.squareup.okhttp.**

# Test for debugging
#-renamesourcefileattribute SourceFile
#-keepattributes SourceFile,LineNumberTable
#-printmapping build/outputs/mapping/release/mapping.txt
#-dontobfuscate

# ?
#-keepattributes InnerClasses

# RxJava
#-dontwarn sun.misc.**
-dontwarn sun.misc.Unsafe
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
   long producerIndex;
   long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# Changelog
-keep class it.gmariotti.changelibs.library.internal.ChangeLogAdapter { *; }

# Joda Time. This is supposedly included in the lib itself(?).
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

# fix for build errors
-keepattributes EnclosingMethod

# Xiaomi issue
-keepnames class org.apache.commons.lang3.** { *; }