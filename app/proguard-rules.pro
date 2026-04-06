# MedVault ProGuard Rules
# Add project-specific ProGuard rules here.

# Keep kotlinx.serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.medvault.**$$serializer { *; }
-keepclassmembers class com.medvault.** {
    *** Companion;
}
-keepclasseswithmembers class com.medvault.** {
    kotlinx.serialization.KSerializer serializer(...);
}
