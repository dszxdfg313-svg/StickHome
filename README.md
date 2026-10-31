# StickHome

واجهة رئيسية بسيطة لأندرويد TV: ساعة وتاريخ كبيران وصف تطبيقات تتنقل بينها بالريموت.

## البناء
**الطريقة 1 (بدون تثبيت شيء):** ارفع المجلد كاملًا إلى مستودع على GitHub، ثم افتح تبويب Actions
وشغّل build-apk، وحمّل الملف stickhome-apk (app-debug.apk) من نتيجة التشغيل.

**الطريقة 2:** افتح المجلد في Android Studio واختر Build > Build APK.

## التثبيت (يحتاج adb)
    adb install -r app-debug.apk
    adb shell cmd package set-home-activity com.example.stickhome/.HomeActivity

عند الضغط على زر Home قد يسألك الجهاز أي شاشة رئيسية تستخدم، اختر StickHome.

## التراجع
    adb uninstall com.example.stickhome
