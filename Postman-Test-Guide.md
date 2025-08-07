# 🚀 Postman Test Kılavuzu

Self-Healing iOS Test Elements servisi için kapsamlı Postman collection hazırladım. Bu kılavuz nasıl test edeceğinizi adım adım anlatıyor.

## 📥 Collection'ı İçe Aktarma

1. **Postman'i açın**
2. **Import** butonuna tıklayın
3. **File** sekmesinde `Self-Healing-iOS-Tests.postman_collection.json` dosyasını seçin
4. **Import** butonuna tıklayın

## 🏃‍♂️ Servisi Başlatma

Testlere başlamadan önce servisi çalıştırın:

```bash
# Maven kurulu ise:
mvn spring-boot:run

# Veya IDE'den SelfHealingLscIosApplication.java'yı run edin
```

Servis `http://localhost:8080` adresinde çalışacak.

## 📋 Test Sırası

### 1️⃣ **Health & Info** (Önce bunları çalıştırın)

#### ✅ Health Check
- **Amaç**: Servisin çalışıp çalışmadığını kontrol eder
- **Beklenen**: `"status": "UP"`
- **İlk test**: Mutlaka bununla başlayın!

#### ⚙️ Configuration  
- **Amaç**: Servis ayarlarını görür
- **Beklenen**: threshold, autoUpdate, maxSuggestions değerleri

#### 📊 Statistics
- **Amaç**: Element sayıları ve dağılımını görür
- **Beklenen**: 100+ element, screen bazında gruplar

### 2️⃣ **Element Search** (Ana özellikler)

#### 🎯 Find Element - Exact Match
- **Test verisi**: `login_submit_button` (elements.json'da mevcut)
- **Beklenen sonuç**: 
```json
{
  "success": true,
  "message": "Element found - exact match",
  "result": {
    "matchType": "EXACT",
    "similarityScore": 1.0,
    "autoUpdated": false
  }
}
```

#### 🔍 Find Element - Similarity Match  
- **Test verisi**: `loginBtn` (benzer ama farklı)
- **Beklenen sonuç**:
```json
{
  "matchType": "SIMILARITY", 
  "similarityScore": 0.8+,
  "autoUpdated": true
}
```

#### 📧 Find Element - Email Field
- **Test verisi**: Email field benzetmesi
- **Beklenen**: `emailTextField` ile eşleşmeli

#### ❌ Find Element - Not Found
- **Test verisi**: Hiç benzemeyen element
- **Beklenen**: `"matchType": "NOT_FOUND"`

### 3️⃣ **Suggestions** (Öneri sistemi)

#### 💡 Get Suggestions - Login Elements
- **Amaç**: Login screen'indeki button'lar için öneriler
- **Beklenen**: 5 öneriye kadar, benzerlik skorları ile

#### 📝 Get Suggestions - Text Field
- **Amaç**: Text field türünde öneriler
- **Beklenen**: TextField tipindeki elementler

### 4️⃣ **Element Management** (Yönetim)

#### ✅ Validate Element - Valid
- **Test verisi**: Geçerli element
- **Beklenen**: `"valid": true, "errors": []`

#### ❌ Validate Element - Invalid  
- **Test verisi**: Eksik alanları olan element
- **Beklenen**: `"valid": false, "errors": [...]`

#### 🔄 Update Element
- **Önkoşul**: Önce "Find Element - Exact Match" çalıştırın
- **Amaç**: Mevcut elementi günceller
- **Beklenen**: `"success": true`

### 5️⃣ **Algorithm Tests** (Algoritma testleri)

#### 🧮 String Similarity Tests
- **High**: `loginButton` vs `loginBtn` → ~82% benzerlik
- **Medium**: `emailTextField` vs `emailField` → ~70% benzerlik  
- **Low**: `loginButton` vs `homeScreen` → ~25% benzerlik

#### 🎯 Element Similarity Tests
- **High**: Benzer elementler → Yüksek skor
- **Different Types**: Button vs TextField → Düşük skor

#### 🛣️ XPath Similarity
- **iOS XPath**: Structural parsing testi
- **Beklenen**: Attribute benzerliği önemli

### 6️⃣ **Sample Data** (Örnek veriler)

#### 📋 Get Sample Element/Elements
- **Amaç**: Test için örnek veriler
- **Kullanım**: Diğer testlerde referans olarak

## 🎯 Beklenen Test Sonuçları

### ✅ Başarılı Senaryolar

```json
// Exact Match
{
  "success": true,
  "message": "Element found - exact match",
  "result": {
    "matchType": "EXACT",
    "similarityScore": 1.0
  }
}

// Similarity Match  
{
  "success": true,
  "message": "Element found - similarity match (82.50%)",
  "result": {
    "matchType": "SIMILARITY",
    "similarityScore": 0.825,
    "autoUpdated": true
  }
}

// String Similarity
{
  "string1": "loginButton",
  "string2": "loginBtn", 
  "similarity": 0.8181818181818182,
  "similarityPercentage": "81.82%"
}
```

### ❌ Not Found Senaryosu

```json
{
  "success": true,
  "message": "Element not found - no suitable match",
  "result": {
    "matchType": "NOT_FOUND",
    "similarityScore": 0.0,
    "matchedElement": null
  }
}
```

## 🔧 Test Environment

Collection'da `baseUrl` değişkeni tanımlı:
- **Default**: `http://localhost:8080`
- **Değiştirmek için**: Collection → Variables → baseUrl

## 📊 Otomatik Test Validasyonları

Her request için otomatik kontroller:
- ✅ Response time < 5000ms
- ✅ Content-Type: application/json  
- ✅ Status code: 200

## 🐛 Troubleshooting

### Yaygın Hatalar

1. **Connection refused**
   - Servisin çalıştığından emin olun: `mvn spring-boot:run`

2. **404 Not Found**
   - URL'lerin doğru olduğunu kontrol edin
   - baseUrl değişkenini kontrol edin

3. **500 Internal Server Error**
   - Console loglarını kontrol edin
   - elements.json dosyasının yüklendiğinden emin olun

4. **Validation errors**
   - JSON body'nin doğru format olduğunu kontrol edin
   - Zorunlu alanların dolu olduğunu kontrol edin

## 🎮 İleri Seviye Testler

### Custom Test Scripts

İsterseniz her request'e özel test scriptleri ekleyebilirsiniz:

```javascript
// Similarity Score Test
pm.test("Similarity score should be above threshold", function () {
    const jsonData = pm.response.json();
    if (jsonData.result && jsonData.result.similarityScore) {
        pm.expect(jsonData.result.similarityScore).to.be.above(0.75);
    }
});

// Auto Update Test  
pm.test("Should auto-update similar elements", function () {
    const jsonData = pm.response.json();
    if (jsonData.result && jsonData.result.matchType === "SIMILARITY") {
        pm.expect(jsonData.result.autoUpdated).to.be.true;
    }
});
```

### Performance Testing

Collection'ı Runner ile çalıştırıp performance ölçebilirsiniz:
1. Collection → Run
2. Iterations: 10  
3. Delay: 500ms
4. Run

## 🏆 Test Completion Checklist

- [ ] Health Check geçti
- [ ] Statistics alındı  
- [ ] Exact match bulundu
- [ ] Similarity match bulundu
- [ ] Not found senaryosu test edildi
- [ ] Öneriler alındı
- [ ] Validation testleri geçti
- [ ] String similarity testleri yapıldı
- [ ] Element similarity testleri yapıldı
- [ ] XPath similarity test edildi
- [ ] Sample data alındı

Tüm testler geçtiğinde self-healing servisiniz hazır! 🎉

---

## 📞 Yardım

Test sırasında sorun yaşarsanız:
1. Servis loglarını kontrol edin
2. Postman Console'u açın (View → Show Postman Console)
3. Network sekmesinden raw request/response'ları inceleyin 