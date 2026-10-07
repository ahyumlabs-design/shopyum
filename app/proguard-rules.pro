# Add project specific R8 rules here.
# Navigation 3, Serialization, Room, Moshi, Retrofit, Play Services, etc.

-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

-keep class com.package1.shopcook.model.** { *; }
-keep class com.package1.shopcook.data.** { *; }
