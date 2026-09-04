# ProGuard / R8 Configuration for Money Manager Ex (AMMX)

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-allowaccessmodification
-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

# Standard Android Entry Points
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends androidx.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.fragment.app.DialogFragment

# Native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Custom Views
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

# Enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# R resources
-keepclassmembers class **.R$* {
    public static <fields>;
}

# MMEX Data Models & Persistent Serialization
-keep class com.money.manager.ex.home.RecentDatabasesProvider { *; }
-keep class com.money.manager.ex.home.DatabaseMetadata { *; }
-keep class com.money.manager.ex.sync.PocketBaseSyncEngine$SyncConfig { *; }
-keep class com.money.manager.ex.sync.PocketBaseSyncEngine$TableConfig { *; }
-keep class com.money.manager.ex.investment.yahoofinance.** { *; }

# Gson
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# Parceler
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep class **$$Parcelable { *; }
-keep class org.parceler.** { *; }
-dontwarn org.parceler.**
-keep @org.parceler.Parcel class * { *; }
-keepclassmembers class * {
    @org.parceler.ParcelProperty <methods>;
    @org.parceler.ParcelProperty <fields>;
}

# Dagger 2
-keepclassmembers class * {
    @javax.inject.Inject *;
}
-keep class **$$ModuleAdapter
-keep class **$$InjectAdapter
-keep class **$$ViewInjector
-keep class dagger.** { *; }
-dontwarn dagger.**

# EventBus 3
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

# SQLCipher / Zetetic
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }
-dontwarn net.sqlcipher.**
-dontwarn net.zetetic.**

# Retrofit, OkHttp, RxJava
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn io.reactivex.**
-dontwarn rx.**

# Charts & UI Libraries
-dontwarn io.realm.**
-keep class com.github.mikephil.charting.** { *; }
-keep class com.mikepenz.iconics.** { *; }
-keep class it.gmariotti.changelibs.library.internal.ChangeLogAdapter { *; }

# External Utilities & Ignore Warnings
-dontwarn com.opencsv.**
-keep class com.opencsv.** { *; }
-keep class net.objecthunter.exp4j.** { *; }
-dontwarn org.jsoup.**
-keep class org.jsoup.** { *; }
-dontwarn com.jakewharton.timber.**
-dontwarn org.apache.**
-dontwarn com.google.common.**
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-dontwarn sun.misc.Unsafe
-keepnames class org.apache.commons.lang3.** { *; }
