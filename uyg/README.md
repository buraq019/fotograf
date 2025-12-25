# Android Gallery Cleaner & Tinder-like Swipe App

Bu proje, galerinizdeki fotoğrafları "Tinder" benzeri bir arayüzle (Sağa kaydır: Tut, Sola kaydır: Sil) organize etmenizi sağlayan modern bir Android uygulamasıdır.

## Özellikler
- **MVVM Mimarisi** & **Clean Architecture** prensipleri.
- **Jetpack Compose** ile tamamen modern UI.
- **MediaStore API**: Cihazdaki fotoğrafları okur.
- **Scoped Storage Uyumluluğu**: Android 10, 11, 12, 13+ sürümlerinde güvenli silme işlemi (`IntentSender` ile kullanıcı onayı).
- **Performans**: `Lazy` yükleme ve Coil ile görsel optimizasyonu.

## Kurulum ve Çalıştırma
1. Android Studio'yu açın.
2. "Open" diyerek bu klasörü (`uyg`) seçin.
3. Gradle senkronizasyonunun bitmesini bekleyin.
4. Cihazınızı bağlayın veya Emulator açın.
5. "Run" (Shift+F10) tuşuna basın.

## Kullanım
1. Uygulama açıldığında **Fotoğraflara Erişim İzni** isteyecektir. "Allow/İzin Ver" deyin.
2. Galerinizdeki fotoğraflar kart olarak karşınıza gelecektir.
3. **Sağa Kaydır (Yeşil)**: Fotoğrafı tutarsınız, istatistik "Kept" (Kalan) artar.
4. **Sola Kaydır (Kırmızı)**: Fotoğrafı silmek için işlem başlatılır.
   - **Android 11+**: Sistem bir onay penceresi ("Allow Gallery Cleaner to move this item to trash?") çıkarır. Onaylarsanız silinir.
5. Tüm fotoğraflar bitince "No photos left" ekranı gelir.

## Teknik Notlar
- **Fotoğraf Silme**: `PhotoRepository` sınıfı, Android sürümüne göre farklı stratejiler izler. Android 10'da `RecoverableSecurityException`, Android 11+'da `createDeleteRequest` kullanılır.
- **UI State**: `MainViewModel`, `StateFlow` kullanarak UI durumunu (Loading, Success, Empty) yönetir.

## Geliştirici
Sizin İçin Hazırlandı!
