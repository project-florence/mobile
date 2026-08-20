# Florence Mobile ProGuard rules
# Kept deliberately minimal and conservative: only what reflection / runtime
# lookup in this app needs. Retrofit (annotation-driven reflection),
# kotlinx.serialization (generated serializers), and Hilt (generated factories).

# ---- Retrofit / OkHttp ----
# Uses generic signatures + annotations (runtime retention) for type/token parsing.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
# Retrofit service methods are dispatched reflectively via @GET/@POST etc.
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep class retrofit2.** { *; }

# ---- kotlinx.serialization ----
# Generated $serializer companion classes + the KSerializer(...) factory must survive.
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.florence.app.**$$serializer { *; }
-keepclassmembers class com.florence.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.florence.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.florence.app.** { *; }

# ---- Hilt ----
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**
-dontwarn javax.annotation.**
-dontwarn javax.inject.**

# ---- errorprone annotations (compile-time only, referenced by Tink/security-crypto) ----
-dontwarn com.google.errorprone.annotations.**

# ---- BuildConfig / metadata ----
-keep class com.florence.app.BuildConfig { *; }
-keepattributes Exceptions, InnerClasses, EnclosingMethod
