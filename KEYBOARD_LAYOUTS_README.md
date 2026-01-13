# GlideIME-Hungarian - Billentyűzetkiosztások

Magyar nyelvi támogatás a Huawei Glide fizikai billentyűzethez Android/HarmonyOS rendszereken.

## Áttekintés

Ez az IME (Input Method Editor) két különböző magyar QWERTZ billentyűzetkiosztást tartalmaz, amelyek között **Shift+Space** kombinációval lehet váltani.

## Kiosztások

### 1. Eredeti Kiosztás
- Alapértelmezett kiosztás
- Huawei Glide PDF alapján
- Hagyományos számsor (0-9)
- Alt+O kombinációval érhető el az "ö" karakter

📄 **Részletes dokumentáció:** [KEYBOARD_LAYOUT_ORIGINAL.md](KEYBOARD_LAYOUT_ORIGINAL.md)

### 2. Alternatív Kiosztás
- Módosított kiosztás az "ö" könnyebb eléréséhez
- **0 billentyű → ö/Ö**
- **ESC billentyű → 0**

📄 **Részletes dokumentáció:** [KEYBOARD_LAYOUT_ALTERNATIVE.md](KEYBOARD_LAYOUT_ALTERNATIVE.md)

## Gyors Összehasonlítás

| Funkció | Eredeti | Alternatív |
|---------|---------|------------|
| 0 billentyű | 0 | ö |
| Shift+0 | § | Ö |
| ESC billentyű | ESC | 0 |
| Váltás | Shift+Space | Shift+Space |

## Magyar Karakterek

Mindkét kiosztásban elérhetők az összes magyar ékezetes karakterek:

| Karakter | Billentyű | Shift |
|----------|-----------|-------|
| á | ' | Á |
| é | ; | É |
| í | Alt+I | Alt+I (Shift) |
| ó | = | Ó |
| ö | Alt+O (eredeti) / 0 (alternatív) | Alt+O (Shift) / Shift+0 |
| ő | [ | Ő |
| ú | ] | Ú |
| ű | \ | Ű |
| ü | - | Ü |

## QWERTZ Layout

A kiosztás **QWERTZ** szabványt követ (nem QWERTY!):
- A fizikai **Y** billentyű → **Z** karaktert ír
- A fizikai **Z** billentyű → **Y** karaktert ír

Ez megfelel a magyar/német billentyűzet szabványnak.

## Billentyűkombinációk

### Módosító billentyűk
- **Shift**: Nagybetűk és alternatív karakterek
- **Alt**: Speciális karakterek (€, @, #, stb.)
- **Shift+Alt**: Ékezetes jelek (´, `, ^, stb.)
- **Caps Lock**: Nagybetűzár
- **Ctrl**: Rendszer parancsok (Ctrl+C, Ctrl+V, stb.)

### Váltás a kiosztások között
- **Shift+Space**: Eredeti ↔ Alternatív kiosztás váltás
- Toast üzenet jelzi melyik kiosztás aktív

## Speciális Karakterek

### Programozói karakterek
- **@**: Alt+V
- **#**: Alt+X
- **$**: Alt+;
- **&**: Alt+C
- **\**: Alt+Q
- **|**: Alt+W
- **[** és **]**: Alt+F, Alt+G
- **{** és **}**: Alt+B, Alt+N

### Pénznemek
- **€**: Alt+U
- **$**: Alt+;

### Diakritikus jelek
- **´** (éles ékezet): Shift+Alt+9
- **˝** (dupla ékezet): Shift+Alt+0 (csak eredeti kiosztásban)
- **`** (tompa ékezet): Shift+Alt+7
- **^** (kalap): Shift+Alt+3
- **~** (tilde): Shift+Alt+1
- **¨** (trema): Shift+Alt+-
- **°** (fok jel): Shift+Alt+5

## Ctrl Billentyűkombinációk

Az összes szabványos Ctrl kombináció működik:

### Szövegszerkesztés
- **Ctrl+C**: Másolás
- **Ctrl+X**: Kivágás
- **Ctrl+V**: Beillesztés
- **Ctrl+A**: Mindet kijelöl
- **Ctrl+Z**: Visszavonás
- **Ctrl+Y**: Újra

### Fájlműveletek
- **Ctrl+S**: Mentés
- **Ctrl+N**: Új
- **Ctrl+O**: Megnyitás
- **Ctrl+P**: Nyomtatás

### Böngésző
- **Ctrl+T**: Új lap
- **Ctrl+W**: Lap bezárása
- **Ctrl+R**: Frissítés
- **Ctrl+F**: Keresés
- **Ctrl+L**: Címsor kijelölése

### Formázás
- **Ctrl+B**: Félkövér
- **Ctrl+I**: Dőlt
- **Ctrl+U**: Aláhúzott

## Kompatibilitás

### Támogatott rendszerek
- ✅ Android 8.0+
- ✅ HarmonyOS 4.3+

### Tesztelt alkalmazások
- ✅ WPS Office
- ✅ Microsoft Excel
- ✅ Google Sheets (böngésző)
- ✅ Nextcloud + OnlyOffice
- ✅ Webes űrlapok
- ✅ Chat alkalmazások
- ✅ OTP mezők (1-5. mező auto-advance)

### OnlyOffice Matepad Pro
A **dba086f** verzió speciális javításokat tartalmaz:
- 50ms karakterdeduplikáció
- 50ms késleltetés az automatikus cellába íráshoz
- Javított karakterduplázás védelem

## OTP (One-Time Password) Támogatás

### Működő funkciók
- ✅ Automatikus továbbítás az 1-5. OTP mezők között
- ✅ Backspace visszalép az előző mezőre
- ✅ Böngésző OTP mezők támogatása

### Korlátozások
- ⚠️ Ügyfélkapu 6. OTP mező auto-submit **nem működik**
  - A weboldal hibásan implementált IME support miatt
  - Az 1-5. mező működik, a 6. mezőnél kézi submit szükséges

## Telepítés

1. Töltsd le az APK-t a [GitHub Releases](https://github.com/Szechko1/GlideIME-Hungarian/releases) oldalról
2. Telepítsd a készülékedre
3. Menj a **Beállítások → Rendszer → Nyelvek és bevitel → Virtuális billentyűzet**
4. Engedélyezd a **GlideIME-Hungarian**-t
5. Válaszd ki alapértelmezett billentyűzetként

## Használat

1. Csatlakoztasd a Huawei Glide billentyűzetet
2. A billentyűzet automatikusan használja az eredeti kiosztást
3. **Shift+Space** váltás az alternatív kiosztásra (ha az "ö" gyakoribb nálad)
4. Használd a magyar karaktereket a fenti táblázatok szerint

## Verziók

### Jelenlegi stabil: dba086f
- ✅ OnlyOffice Matepad Pro javítások
- ✅ Debug logging az OTP működéshez
- ✅ Karakterdeduplikáció javítások
- ✅ WPS, Excel, Google Sheets támogatás

## Hibajelentés

Ha problémát tapasztalsz, kérlek nyiss egy [issue-t](https://github.com/Szechko1/GlideIME-Hungarian/issues) a következő információkkal:
- Készülék típusa és OS verzió
- APK verzió (commit hash)
- Alkalmazás ahol a probléma jelentkezik
- Reprodukálási lépések
- Toast üzenetek (ha vannak debug üzenetek)

## Fejlesztés

A projekt Kotlin nyelven íródott, Android Studio-ban fejleszthető.

### Build
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

### GitHub Actions
Automatikus APK build minden push után a `claude/**` ágakon.

## Licenc

Nyílt forráskódú projekt magyar felhasználók számára.

## Köszönetnyilvánítás

- Huawei Glide Keyboard hardver
- HarmonyOS 4.3 IME API
- Android InputMethodService framework
