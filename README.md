# Self-Healing iOS Test Elements Service

Bu proje iOS native test elementleri için self-healing (kendini onaran) bir Spring Boot web servisidir. Levenshtein Distance ve LCS (Longest Common Subsequence) algoritmalarını kullanarak test elementlerini karşılaştırır ve en yakın eşleşmeleri bulur.

## 🚀 Özellikler

- **Element Karşılaştırma**: iOS test elementlerini çoklu kriterlere göre karşılaştırır
- **Levenshtein Distance**: String'ler arası edit mesafesi hesaplama
- **LCS Algorithm**: En uzun ortak alt dizi bulma
- **Self-Healing**: Otomatik element güncelleme
- **REST API**: Tüm işlevler REST endpoint'leri ile erişilebilir
- **Similarity Threshold**: Konfigüre edilebilir benzerlik eşiği
- **Auto-Update**: Otomatik güncelleme özelliği
- **Multiple Suggestions**: Birden fazla öneri getirme

## 🛠️ Teknolojiler

- **Java 17**
- **Spring Boot 3.2.0**
- **Maven**
- **Jackson (JSON Processing)**
- **Apache Commons Lang3**
- **SLF4J Logging**

## 📋 Gereksinimler

- Java 17 veya üzeri
- Maven 3.6+
- En az 512MB RAM

## 🔧 Kurulum

1. **Projeyi klonlayın:**
```bash
git clone <repository-url>
cd selfhealing-lsc-ios
```

2. **Maven ile derleyin:**
```bash
mvn clean compile
```

3. **Uygulamayı çalıştırın:**
```bash
mvn spring-boot:run
```

Uygulama varsayılan olarak `http://localhost:8080` adresinde başlar.

## 📖 API Kullanımı

### 1. Element Arama
**POST** `/api/v1/self-healing/find`

Test verisi gönderip elements.json içinde arama yapar.

```json
{
  "element_id": "test_login_button",
  "accessibility_id": "loginButton",
  "name": "loginButton",
  "xpath": "//XCUIElementTypeButton[@name='loginButton']",
  "class_name": "XCUIElementTypeButton",
  "screen": "LoginScreen",
  "element_type": "button"
}
```

**Yanıt:**
```json
{
  "success": true,
  "message": "Element found - similarity match (85.50%)",
  "result": {
    "originalElement": {...},
    "matchedElement": {...},
    "similarityScore": 0.855,
    "matchType": "SIMILARITY",
    "autoUpdated": true
  }
}
```

### 2. Öneriler Alma
**POST** `/api/v1/self-healing/suggestions`

Birden fazla benzer element önerisi alır.

### 3. Element Güncelleme
**PUT** `/api/v1/self-healing/update/{oldElementId}`

Varolan bir elementi yeni veriyle günceller.

### 4. Element Validasyonu
**POST** `/api/v1/self-healing/validate`

Element verisinin geçerliliğini kontrol eder.

### 5. Sistem İstatistikleri
**GET** `/api/v1/self-healing/stats`

Sistem ve element istatistikleri.

### 6. Health Check
**GET** `/api/v1/self-healing/health`

Servisin durumunu kontrol eder.

## 🧪 Test Endpoint'leri

### String Benzerlik Testi
**POST** `/api/v1/test/string-similarity`

```json
{
  "string1": "loginButton",
  "string2": "loginBtn"
}
```

### Element Benzerlik Testi
**POST** `/api/v1/test/element-similarity`

İki elementi karşılaştırır.

### XPath Benzerlik Testi
**POST** `/api/v1/test/xpath-similarity`

XPath string'lerini karşılaştırır.

## ⚙️ Konfigürasyon

`src/main/resources/application.properties` dosyasından ayarları değiştirebilirsiniz:

```properties
# Benzerlik eşiği (0.0-1.0)
selfhealing.similarity.threshold=0.75

# Otomatik güncelleme
selfhealing.auto-update.enabled=true

# Maksimum öneri sayısı
selfhealing.max-suggestions=5
```

## 🔍 Algoritma Detayları

### Levenshtein Distance
- İki string arasındaki minimum edit mesafesini hesaplar
- Insertion, deletion, substitution operasyonlarını dikkate alır
- Dynamic programming ile O(m*n) kompleksitede çalışır

### LCS (Longest Common Subsequence)
- İki string'in en uzun ortak alt dizisini bulur
- Karakter sırasını koruyarak benzerlik hesaplar
- Özellikle XPath ve structure karşılaştırmalarında etkili

### Element Comparison Weighted Scoring
- **Accessibility ID**: %25 ağırlık (iOS için en kritik)
- **Name**: %25 ağırlık
- **XPath**: %20 ağırlık
- **Class Name**: %15 ağırlık
- **Screen**: %10 ağırlık
- **Element Type**: %5 ağırlık

## 📊 Örnek Kullanım Senaryoları

### Senaryo 1: Tam Eşleşme
Gönderilen element tam olarak elements.json'da bulunur.
- **Sonuç**: `matchType: "EXACT"`, `similarity: 1.0`

### Senaryo 2: Benzerlik Eşleşmesi
Element bulunamaz ama benzer bir element vardır (threshold üzeri).
- **Sonuç**: `matchType: "SIMILARITY"`, otomatik güncelleme

### Senaryo 3: Element Bulunamadı
Hiçbir benzer element threshold'u geçemez.
- **Sonuç**: `matchType: "NOT_FOUND"`

## 🔒 Güvenlik

- CORS destekli (tüm origin'lere açık - production'da kısıtlanmalı)
- Input validation
- Error handling
- Logging

## 📈 Performans

- Element cache (1 dakika)
- Efficient string algorithms
- Memory-optimized comparisons
- Configurable suggestion limits

## 🐛 Troubleshooting

### Yaygın Sorunlar

1. **Elements.json yüklenemiyor**
   - Dosyanın `src/main/resources/` klasöründe olduğundan emin olun
   - JSON formatının geçerli olduğunu kontrol edin

2. **Benzerlik skorları düşük**
   - Threshold değerini düşürün
   - Element attribute'larını kontrol edin

3. **Memory issues**
   - JVM heap size'ı artırın: `-Xmx1024m`

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/YeniOzellik`)
3. Commit yapın (`git commit -am 'Yeni özellik eklendi'`)
4. Push yapın (`git push origin feature/YeniOzellik`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.

## 📞 İletişim

Herhangi bir sorunuz için issue açabilir veya doğrudan iletişime geçebilirsiniz.

---

**Not**: Bu servis iOS XCUITest framework'ü için özel olarak geliştirilmiştir. Android elementleri için farklı adaptasyonlar gerekebilir. 