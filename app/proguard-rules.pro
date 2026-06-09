# Стандардни ProGuard правила за TaskMate.
# Room и Firebase класите се чуваат автоматски преку нивните consumer-rules.
-keepattributes Signature
-keepattributes *Annotation*

# Чувај ги Task data класите (се користат од Firestore рефлексија)
-keep class com.taskmate.app.data.local.** { *; }
