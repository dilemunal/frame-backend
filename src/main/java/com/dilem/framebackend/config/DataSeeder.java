package com.dilem.framebackend.config;

import com.dilem.framebackend.model.JournalEntry;
import com.dilem.framebackend.model.User;
import com.dilem.framebackend.model.enums.AuthProvider;
import com.dilem.framebackend.repository.JournalEntryRepository;
import com.dilem.framebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Create ghost user first to occupy ID=1
        if (userRepository.findByEmail("ghost@frame.internal").isEmpty()) {
            userRepository.save(User.builder()
                    .firstname("Ghost")
                    .lastname("User")
                    .email("ghost@frame.internal")
                    .password(passwordEncoder.encode("ghost123"))
                    .provider(AuthProvider.LOCAL)
                    .build());
        }

        Optional<User> existingTestUser = userRepository.findByEmail("test@frame.app");
        if (existingTestUser.isPresent()) {
            // Şifreyi zorla "test123" yap ki eski denemelerden kalan farklı şifreler sorun yaratmasın
            User user = existingTestUser.get();
            user.setPassword(passwordEncoder.encode("test123"));
            userRepository.save(user);
            System.out.println("[DataSeeder] ✅ test@frame.app kullanıcısının şifresi 'test123' olarak sıfırlandı.");
            return; // Veriler zaten eklenmiş, tekrar ekleme.
        }

        User seedUser = userRepository.save(User.builder()
                .firstname("Dilem")
                .lastname("Test")
                .email("test@frame.app")
                .password(passwordEncoder.encode("test123"))
                .provider(AuthProvider.LOCAL)
                .build());

        List<JournalEntry> entries = buildEntries(seedUser);
        journalEntryRepository.saveAll(entries);

        System.out.println("[DataSeeder] ✅ " + entries.size() + " dummy entry oluşturuldu → user: test@frame.app / test123");
    }

    private List<JournalEntry> buildEntries(User user) {
        List<JournalEntry> entries = new ArrayList<>();

        // 0: daysAgo=2
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(2))
                .mood("😊")
                .freeText("Bugün inanılmaz bir gün geçirdim. Sabah kahvemi içerken dışarıdan gelen yağmur sesi beni çok huzurlu hissettirdi.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":14,\"desc\":\"Yağmurlu\",\"icon\":\"🌧️\"}")
                .isCapsuleSealed(false).build());

        // 1: daysAgo=5
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(5))
                .mood("😔").title("Zor bir gün")
                .freeText("Her şey ters gitti bugün. Toplantı kötü geçti, eve yorgun döndüm. Ama akşam bir fincan çay her şeyi düzeltti.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":18,\"desc\":\"Bulutlu\",\"icon\":\"☁️\"}")
                .isCapsuleSealed(false).build());

        // 2: daysAgo=11
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(11))
                .mood("🔥")
                .freeText("Yeni projeye başladım. Motivasyonum tavanda. Flutter ile neler yapabileceğimi düşündükçe heyecanlanıyorum.")
                .locationJson("{\"lat\":39.92,\"lng\":32.85,\"name\":\"Ankara\"}")
                .weatherJson("{\"temp\":22,\"desc\":\"Güneşli\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 3: daysAgo=18
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(18))
                .mood("🌙").title("Gece düşünceleri")
                .freeText("Gece yarısı uyuyamadım. Zihnim susmak bilmiyordu. Bazen sessizliğin kendisi çok gürültülü geliyor.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":11,\"desc\":\"Açık\",\"icon\":\"🌙\"}")
                .isCapsuleSealed(false).build());

        // 4: daysAgo=24
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(24))
                .mood("😴")
                .freeText("Hafta sonu evden çıkmadım. Kitap okudum, uyudum, tekrar kitap okudum. Mükemmel bir cumartesi.")
                .weatherJson("{\"temp\":9,\"desc\":\"Sisli\",\"icon\":\"🌫️\"}")
                .isCapsuleSealed(false).build());

        // 5: daysAgo=30
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(30))
                .mood("🌿").title("Doğa yürüyüşü")
                .freeText("Belgrad Ormanı'nda uzun bir yürüyüş yaptık. Akciğerlerim temizlendi, zihin duruldu. Şehirden kaçmak gerekiyor arada.")
                .locationJson("{\"lat\":41.17,\"lng\":28.95,\"name\":\"Belgrad Ormanı\"}")
                .weatherJson("{\"temp\":16,\"desc\":\"Parçalı bulutlu\",\"icon\":\"⛅\"}")
                .isCapsuleSealed(false).build());

        // 6: daysAgo=38
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(38))
                .mood("🎉").title("Doğum günü!")
                .freeText("Bugün 25 yaşına girdim. Arkadaşlarım sürpriz pasta getirdi. Yıllar geçiyor ama insan hâlâ çocuk gibi hissedebiliyor.")
                .locationJson("{\"lat\":41.04,\"lng\":28.99,\"name\":\"Karaköy\"}")
                .weatherJson("{\"temp\":20,\"desc\":\"Güneşli\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 7: daysAgo=45
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(45))
                .mood("😤")
                .freeText("Bugün çok sinirlendim. Neden insanlar bu kadar ilgisiz olabiliyor? Kendimi sakinleştirmek için müzik açtım.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":17,\"desc\":\"Bulutlu\",\"icon\":\"☁️\"}")
                .isCapsuleSealed(false).build());

        // 8: daysAgo=52 (Capsule)
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(52))
                .mood("💡").title("Yeni fikir")
                .freeText("Sabah duşta aklıma geldi: uygulama için film şeridi görünümü. Hemen not aldım. En iyi fikirler her zaman beklenmedik anlarda çıkıyor.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":19,\"desc\":\"Açık\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(true).capsuleUnlockAt(LocalDateTime.now().plusDays(30)).build());

        // 9: daysAgo=61
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(61))
                .mood("🧘").title("Meditasyon")
                .freeText("İlk kez düzenli meditasyon yapmaya başladım. 10 dakika bile fark yaratıyor. Sabahları daha berrak düşünüyorum.")
                .weatherJson("{\"temp\":13,\"desc\":\"Yağmurlu\",\"icon\":\"🌧️\"}")
                .isCapsuleSealed(false).build());

        // 10: daysAgo=74
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(74))
                .mood("✈️").title("Yolculuk başlıyor")
                .freeText("Yarın İzmir'e gidiyorum. Bavulumu hazırladım, heyecandan uyuyamıyorum. Deniz özlemi had safhaya ulaştı.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":21,\"desc\":\"Açık\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 11: daysAgo=80
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(80))
                .mood("🌊").title("İzmir")
                .freeText("Ege'nin rengi başka. Deniz turuncuya döndüğünde sahilde oturdum, hiçbir şey düşünmeden. Huzurun kendisi bu.")
                .locationJson("{\"lat\":38.42,\"lng\":27.14,\"name\":\"İzmir\"}")
                .weatherJson("{\"temp\":28,\"desc\":\"Güneşli\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 12: daysAgo=92
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(92))
                .mood("📚").title("Okuma maratonu")
                .freeText("3 günde 2 kitap bitirdim. 'Suç ve Ceza'yı sonunda okudum. Raskolnikov'un iç sesi o kadar gerçek ki rahatsız edici.")
                .weatherJson("{\"temp\":16,\"desc\":\"Bulutlu\",\"icon\":\"☁️\"}")
                .isCapsuleSealed(false).build());

        // 13: daysAgo=105
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(105))
                .mood("😊")
                .freeText("Eski bir arkadaşımla yıllarca sonra buluştuk. Hiç vakit geçmemiş gibi hissettirdi. Gerçek dostluk buymuş.")
                .locationJson("{\"lat\":41.03,\"lng\":28.98,\"name\":\"Nişantaşı\"}")
                .weatherJson("{\"temp\":12,\"desc\":\"Parçalı bulutlu\",\"icon\":\"⛅\"}")
                .isCapsuleSealed(false).build());

        // 14: daysAgo=120
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(120))
                .mood("🏃").title("Koşu rekoru")
                .freeText("Bugün 10km koştum! İlk kez bu kadar uzun koşabildim. Bacaklarım bitik ama kendimle gurur duyuyorum.")
                .locationJson("{\"lat\":41.05,\"lng\":29.02,\"name\":\"Fenerbahçe Parkı\"}")
                .weatherJson("{\"temp\":15,\"desc\":\"Açık\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 15: daysAgo=138
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(138))
                .mood("🌧️").title("Kış geldi")
                .freeText("İlk kar yağışı. Pencereden izledim. Kar yağarken her şey sessizleşiyor, dünya beyaz bir örtüyle örtünüyor.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":2,\"desc\":\"Karlı\",\"icon\":\"❄️\"}")
                .isCapsuleSealed(false).build());

        // 16: daysAgo=155
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(155))
                .mood("😔")
                .freeText("Yılın bu zamanları her zaman ağır geçiyor. Güneş az, gece uzun. Ama bu da geçecek.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":5,\"desc\":\"Bulutlu\",\"icon\":\"☁️\"}")
                .isCapsuleSealed(false).build());

        // 17: daysAgo=170
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(170))
                .mood("🎵").title("Konser gecesi")
                .freeText("Canlı müzik dinlemenin tadına doyum olmuyor. Salon doluydu, ses kalitesi harikaydı. Bir süre hayattan kopuk hissettim.")
                .locationJson("{\"lat\":41.03,\"lng\":28.99,\"name\":\"Zorlu PSM\"}")
                .weatherJson("{\"temp\":8,\"desc\":\"Açık\",\"icon\":\"🌙\"}")
                .isCapsuleSealed(false).build());

        // 18: daysAgo=190
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(190))
                .mood("💼").title("İş teklifi")
                .freeText("Bugün beklenmedik bir iş teklifi aldım. Kafam karışık. Değişim korkutsa da heyecan verici.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":10,\"desc\":\"Sisli\",\"icon\":\"🌫️\"}")
                .isCapsuleSealed(false).build());

        // 19: daysAgo=210
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(210))
                .mood("🌅")
                .freeText("Boğaz'da gün batımı izledim. Turuncu, mor, pembe — kelimeler yetmiyor. Bazı anlar fotoğrafa da sığmıyor.")
                .locationJson("{\"lat\":41.08,\"lng\":29.05,\"name\":\"Çengelköy\"}")
                .weatherJson("{\"temp\":24,\"desc\":\"Açık\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 20: daysAgo=235
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(235))
                .mood("😊").title("Yemek keyfi")
                .freeText("Annemin yaptığı mantıyı yedim. Ev yemeklerinin bir terapisi var. Şehrin gürültüsünden uzak, masada aile.")
                .locationJson("{\"lat\":40.76,\"lng\":30.40,\"name\":\"Adapazarı\"}")
                .weatherJson("{\"temp\":26,\"desc\":\"Sıcak\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 21: daysAgo=260
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(260))
                .mood("🌿").title("Bahçe")
                .freeText("Sabah erkenden kalktım, balkonun küçük saksı bahçesiyle uğraştım. Toprakla temas bir şekilde sakinleştiriyor.")
                .weatherJson("{\"temp\":18,\"desc\":\"Açık\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 22: daysAgo=300
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(300))
                .mood("🔥").title("Milestone!")
                .freeText("Uygulama ilk 100 kullanıcıya ulaştı. Küçük bir rakam ama büyük bir his. Emek boşa gitmedi.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":22,\"desc\":\"Güneşli\",\"icon\":\"☀️\"}")
                .isCapsuleSealed(false).build());

        // 23: daysAgo=365
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(365))
                .mood("🎊").title("Bir yıl geçti")
                .freeText("Tam bir yıl önce bu günlüğü tutmaya başladım. Geçen yıla baktığımda kendimi tanıyamıyorum — ama iyi bir anlamda.")
                .locationJson("{\"lat\":41.01,\"lng\":28.97,\"name\":\"İstanbul\"}")
                .weatherJson("{\"temp\":7,\"desc\":\"Bulutlu\",\"icon\":\"☁️\"}")
                .isCapsuleSealed(false).build());

        // 24: daysAgo=540
        entries.add(JournalEntry.builder().user(user)
                .createdAt(LocalDateTime.now().minusDays(540))
                .mood("🌙").title("Eski bir not")
                .freeText("O günleri hatırlamak garip. Dünya farklıydı, ben farklıydım. Değişim acı verir ama büyütür.")
                .isCapsuleSealed(false).build());

        return entries;
    }
}
