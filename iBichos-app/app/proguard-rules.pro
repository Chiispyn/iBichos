# ProGuard Rules for iBichos Android App

# 1. Gson rules
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.cetecom.ibichos.data.model.** { *; }
-keep class com.cetecom.ibichos.domain.model.** { *; }

# 2. Retrofit rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# 3. Hilt / Dagger rules (usually bundled, but good to ensure)
-keep class dagger.hilt.** { *; }
-keep class com.cetecom.ibichos.di.** { *; }

# 4. OSMdroid (OpenStreetMap) rules
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# 5. Cloudinary rules
-keep class com.cloudinary.** { *; }
-dontwarn com.cloudinary.**

# 6. Firebase / Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
