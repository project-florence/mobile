# Florence Mobile ProGuard rules
# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# kotlinx.serialization
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
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
