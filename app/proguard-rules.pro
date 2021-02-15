-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepattributes JavascriptInterface
-keepattributes *Annotation*
-keep class androidx.appcompat.widget.** { *; }

-dontwarn com.razorpay.**
-keep class com.razorpay.** {*;}

-optimizations !method/inlining/*

-keepclasseswithmembers class * {
  public void onPayment*(...);
}

# Rules for Instamojo SDK
-keep class com.instamojo.android.**{*;}