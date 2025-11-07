# GlideIME-Hungarian 🇭🇺

Magyar billentyűzet-kiosztás (QWERTZ) Huawei Glide fizikai billentyűzethez HarmonyOS/Android rendszereken.

## 📝 Leírás

A **GlideIME-Hungarian** egy Input Method Editor (IME) alkalmazás, amely lehetővé teszi a magyar QWERTZ billentyűzet-kiosztás használatát Huawei készülékeken fizikai billentyűzettel (különösen Huawei Glide). Az alkalmazás támogatja az összes magyar ékezetes karaktert, valamint számos speciális funkciót, mint például a Ctrl-kombinációk, szövegkezelési parancsok és intelligens Enter kezelés.

## ✨ Funkciók

### Billentyűzet-kiosztás
- ✅ **Magyar QWERTZ kiosztás** teljes támogatással
- ✅ **Magyar ékezetes karakterek**: á, é, í, ó, ö, ő, ú, ü, ű
- ✅ **Speciális karakterek** Alt és Shift+Alt kombinációkkal
- ✅ **Shift és CapsLock** támogatás
- ✅ **Számok és szimbólumok** teljes magyar kiosztás szerint
- ✅ **Dual Layout (Magyar-1 és Magyar-2)** - Két kiosztás közötti váltás lehetősége

### Dual Layout - Magyar-1 és Magyar-2
Az alkalmazás két billentyűzet kiosztást kínál, amelyek között **Shift+Space** kombinációval lehet váltani:

**Magyar-1 (alapértelmezett):**
- ESC gomb: ESC funkció
- 0 gomb: 0 szám

**Magyar-2 (módosított):**
- ESC gomb: 0 szám
- 0 gomb: ö (kisbetű), Ö (Shift/Caps Lock)

A layout váltáskor egy értesítés jelenik meg, amely mutatja az aktív kiosztást. Az alkalmazás mindig Magyar-1 kiosztással indul.

**⚠️ Ismert korlát - Microsoft Word 365:**
Magyar-2 layout esetén az ESC gomb kiírja a "0" számot, de Microsoft Word-ben továbbra is kilép a dokumentumból. Ez az Android/HarmonyOS rendszer korlátja: a hardware billentyűzet eseményeit közvetlenül az alkalmazás kapja meg, és az IME nem tudja teljesen blokkolni őket. A probléma csak MS Word agresszív ESC kezelése miatt jelentkezik.
- **Megoldás:** Használj Magyar-1 layout-ot MS Word-ben, vagy térképezd át az ESC funkciót a Word beállításaiban
- **Más alkalmazások:** WPS Office és a legtöbb egyéb app helyesen működik ✓

### Szövegkezelés
- ✅ **Ctrl+C/V/X** - Másolás, beillesztés, kivágás
- ✅ **Ctrl+Z/Y** - Visszavonás, Mégis
- ✅ **Ctrl+A** - Minden kijelölése
- ✅ **Shift+Nyíl** - Szöveg kijelölése
- ✅ **Ctrl+Backspace/Delete** - Szó törlése
- ✅ **Ctrl+Left/Right** - Szó szerinti navigáció
- ✅ **Ctrl+Home/End** - Dokumentum eleje/vége

### Alkalmazás műveletek
- ✅ **Ctrl+S** - Mentés
- ✅ **Ctrl+F** - Keresés
- ✅ **Ctrl+H** - Csere
- ✅ **Ctrl+N** - Új
- ✅ **Ctrl+O** - Megnyitás
- ✅ **Ctrl+P** - Nyomtatás
- ✅ **Ctrl+W** - Bezárás
- ✅ **Ctrl+T** - Új lap
- ✅ **Ctrl+R** - Frissítés
- ✅ **Ctrl+B/I/U** - Félkövér/Dőlt/Aláhúzott

### Intelligens funkciók
- ✅ **Intelligens Enter** - Többsoros mezőkben új sort, egysoros mezőkben akciót hajt végre
- ✅ **Tab, Escape, Insert** - Teljes támogatás
- ✅ **Page Up/Down, Home/End** - Navigációs billentyűk
- ✅ **Nyíl billentyűk** - Shift kombinációval is

## 📋 Követelmények

- **Android 7.0 (API 24)** vagy újabb
- **Huawei készülék** fizikai billentyűzettel (pl. Huawei Glide)
- **HarmonyOS** vagy Android operációs rendszer

## 🛠️ Build Instrukciók

### Előfeltételek

1. **Android Studio** - Töltsd le és telepítsd az [Android Studio](https://developer.android.com/studio)-t
2. **JDK 11** vagy újabb - Az Android Studio tartalmazza
3. **Android SDK** - Az Android Studio-val automatikusan települ
   - Minimum SDK: API 24 (Android 7.0)
   - Target SDK: API 36

### Build Android Studio-val

1. **Klónozd le a repository-t**:
   ```bash
   git clone https://github.com/Szechko1/GlideIME-Hungarian.git
   cd GlideIME-Hungarian
   ```

2. **Nyisd meg Android Studio-ban**:
   - Indítsd el az Android Studio-t
   - Válaszd: `File` → `Open`
   - Navigálj a `GlideIME-Hungarian` mappához és nyisd meg

3. **Szinkronizáld a Gradle fájlokat**:
   - Android Studio automatikusan felkínálja a Gradle szinkronizálást
   - Vagy: `File` → `Sync Project with Gradle Files`

4. **Build-eld a projektet**:
   - **Debug APK**: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - **Release APK**: `Build` → `Generate Signed Bundle / APK`

5. **A generált APK helye**:
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

### Build parancssorból

#### Windows:
```cmd
cd GlideIME-Hungarian
gradlew.bat assembleDebug
```

#### Linux/Mac:
```bash
cd GlideIME-Hungarian
./gradlew assembleDebug
```

**Release build** (aláírt verzió nélkül):
```bash
./gradlew assembleRelease
```

**Generált APK helye**:
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
```

### APK aláírása (Release)

Release verzióhoz szükséged lesz egy keystore fájlra:

```bash
# Keystore létrehozása (egyszer):
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# Build aláírással:
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=my-release-key.jks \
  -Pandroid.injected.signing.store.password=your-password \
  -Pandroid.injected.signing.key.alias=my-key-alias \
  -Pandroid.injected.signing.key.password=your-password
```

## 📲 Telepítési Útmutató

### 1. Telepítés APK-ból

1. **APK másolása készülékre**:
   - Másold az `app-debug.apk` vagy `app-release.apk` fájlt a készülékedre
   - USB kábellel vagy felhő szolgáltatás segítségével (Google Drive, stb.)

2. **Ismeretlen források engedélyezése**:
   - Nyisd meg: `Beállítások` → `Biztonság`
   - Engedélyezd: `Ismeretlen forrásból származó alkalmazások telepítése`
   - Vagy app-specifikusan: engedélyezd a fájlkezelőnek/böngészőnek

3. **APK telepítése**:
   - Nyisd meg a fájlkezelőt
   - Navigálj az APK fájlhoz
   - Koppints rá és válaszd: `Telepítés`

### 2. IME Aktiválása

1. **Nyisd meg a Glide IME alkalmazást**:
   - Keresd meg az app launcherben: `Glide IME`
   - Nyisd meg az alkalmazást

2. **Aktiváld az IME-t**:
   - Koppints a `Glide IME Aktiválása` gombra
   - A rendszer átnavigál a `Nyelvek és bevitel` beállításokhoz
   - Keresd meg és engedélyezd: **Huawei Glide Magyar Billentyűzet**
   - Fogadd el a biztonsági figyelmeztetést (ez normális IME-knél)

3. **Állítsd be alapértelmezett billentyűzetnek**:
   - Koppints az `IME Beállítások Megnyitása` gombra
   - Vagy manuálisan: `Beállítások` → `Rendszer` → `Nyelvek és bevitel` → `Alapértelmezett billentyűzet`
   - Válaszd ki: **Huawei Glide Magyar Billentyűzet**

### 3. Használat

1. **Nyiss meg bármely alkalmazást**, ahol szöveget írhatsz (Notes, Messages, Browser, stb.)
2. **Koppints egy szövegmezőre**
3. **A fizikai billentyűzet** már magyar kiosztással működik!

### Tipp: Billentyűzet váltás

Ha több billentyűzeted van:
- **Billentyűzet ikon** a navigációs sávon (általában a képernyő alján)
- Vagy: **Swipe** a space billentyűn (néhány eszközön)

## ⌨️ Magyar Billentyűzet-kiosztás

![Magyar Billentyűzet Kiosztás](docs/images/keyboard-layout.jpg)

*A Huawei Glide magyar QWERTZ billentyűzet kiosztása*

### Számok (Shift kombinációval)
```
1'  2"  3+  4!  5%  6/  7=  8(  9)  0§  üÜ  óÓ
```

### QWERTZ sor
```
q  w  e  r  t  z  u  i  o  p  őŐ  úÚ
```

### ASDF sor
```
a  s  d  f  g  h  j  k  l  éÉ  áÁ  űŰ
```

### YXCV sor
```
y  x  c  v  b  n  m  ,?  .:  -_
```

### Alt kombinációk (speciális karakterek)

**Alt + billentyű** funkciók:
- `Alt+Q` = `\`
- `Alt+W` = `|`
- `Alt+E` = `Ä`
- `Alt+U` = `€`
- `Alt+I` = `í`
- `Alt+O` = `ö`, `Shift+Alt+O` = `Ö`
- `Alt+A` = `ä`
- `Alt+S` = `đ`
- `Alt+D` = `Đ`
- `Alt+F` = `[`
- `Alt+G` = `]`
- `Alt+J` = `í`
- `Alt+K` = `ł`
- `Alt+L` = `Ł`
- `Alt+;` = `$`
- `Alt+'` = `ß`
- `Alt+X` = `#`
- `Alt+V` = `@`
- `Alt+B` = `{`
- `Alt+N` = `}`
- `Alt+M` = `µ`
- És még sok más...

### Ékezetek és diakritikus jelek (Shift+Alt+Szám)
```
Shift+Alt+1 = ~
Shift+Alt+2 = ˇ (haček)
Shift+Alt+3 = ^
Shift+Alt+4 = ˘ (bréve)
Shift+Alt+5 = °
Shift+Alt+6 = ˛ (ogonek)
Shift+Alt+7 = `
Shift+Alt+8 = ˙
Shift+Alt+9 = ´
Shift+Alt+0 = ˝
```

## 🔧 Fejlesztés

### Projekt struktúra
```
GlideIME-Hungarian/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/huawei/glideime/
│   │   │   │   ├── GlideIMEService.kt    # Fő IME szolgáltatás
│   │   │   │   └── MainActivity.kt        # Aktiválási UI
│   │   │   ├── res/                       # Erőforrások
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

### Technológiák
- **Nyelv**: Kotlin
- **Min SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Build rendszer**: Gradle 8.x (Kotlin DSL)
- **Dependencies**:
  - AndroidX Core KTX
  - AndroidX AppCompat
  - Material Components

### Hozzájárulás

Szívesen fogadok hozzájárulásokat! Ha hibát találsz vagy új funkciót szeretnél javasolni:

1. Fork-old a repository-t
2. Hozz létre egy feature branch-et (`git checkout -b feature/amazing-feature`)
3. Commit-old a változtatásokat (`git commit -m 'Add some amazing feature'`)
4. Push-old a branch-re (`git push origin feature/amazing-feature`)
5. Nyiss egy Pull Request-et

## 📄 Licenc

Ez a projekt MIT licensz alatt van. További információ: [LICENSE](LICENSE) fájl.

```
MIT License

Copyright (c) 2025 Szechko1

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

## 🐛 Hibajelentés

Ha problémát tapasztalsz, kérlek nyiss egy [Issue](https://github.com/Szechko1/GlideIME-Hungarian/issues)-t a GitHub-on, és add meg a következő információkat:

- Készülék típusa (pl. Huawei Glide)
- Android/HarmonyOS verzió
- A probléma részletes leírása
- Lépések a probléma reprodukálásához
- Képernyőkép (ha lehetséges)

## 📞 Kapcsolat

- **GitHub**: [@Szechko1](https://github.com/Szechko1)
- **Project Link**: [https://github.com/Szechko1/GlideIME-Hungarian](https://github.com/Szechko1/GlideIME-Hungarian)

## 🙏 Köszönetnyilvánítás

- A Huawei közösségnek a fizikai billentyűzet támogatásért
- Minden hozzájárulónak és tesztelőnek

---

**Készítette**: Szechko1
**Verzió**: 1.0.0
**Utolsó frissítés**: 2025. október
