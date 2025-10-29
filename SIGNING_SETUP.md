# APK Aláírás Beállítása - Útmutató

Ez az útmutató lépésről lépésre megmutatja, hogyan állítsd be az automatikus APK aláírást a GitHub Actions-ben.

## 1. lépés: Keystore generálása

A keystore egy fájl, amely tartalmazza az aláíró kulcsodat. **Egyszer** kell létrehoznod, aztán évekig használhatod.

### Windows (PowerShell vagy CMD):

```cmd
keytool -genkey -v -keystore glideime-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias glideime
```

### Linux/Mac (Terminal):

```bash
keytool -genkey -v -keystore glideime-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias glideime
```

### Kérdések amiket feltesz:

1. **Enter keystore password**: Adj meg egy erős jelszót (pl. `MySecurePassword123`)
   - **⚠️ FONTOS**: Jegyezd fel ezt a jelszót! Szükséged lesz rá később.

2. **Re-enter new password**: Ismételd meg a jelszót

3. **What is your first and last name?**: Írd be a neved (pl. `Szechko1`)

4. **What is the name of your organizational unit?**: Enter (vagy pl. `Development`)

5. **What is the name of your organization?**: Enter (vagy pl. `GlideIME`)

6. **What is the name of your City or Locality?**: Enter (vagy városod neve)

7. **What is the name of your State or Province?**: Enter (vagy megyéd neve)

8. **What is the two-letter country code?**: `HU` (Magyarország)

9. **Is this correct?**: `yes`

10. **Enter key password**: Enter (használja ugyanazt mint a keystore password)

### Eredmény:

Létrejött egy `glideime-release.jks` fájl a jelenlegi könyvtárban.

**⚠️ ŐRIZD MEG BIZTONSÁGOSAN!**
- Ez a fájl aláírja az APK-idat
- Ha elveszíted, nem tudsz frissítést kiadni az alkalmazásodhoz
- Ne add ki senkinek!
- Mentsd el biztonságos helyre (pl. password manager, titkosított backup)

## 2. lépés: Keystore konvertálása Base64-re

A GitHub nem tud bináris fájlokat tárolni a Secrets-ben, ezért Base64 formátumra kell konvertálni.

### Windows (PowerShell):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("glideime-release.jks")) | Out-File -Encoding ASCII keystore.txt
```

Vagy ha ez nem működik:

```cmd
certutil -encode glideime-release.jks keystore.txt
```

Majd nyisd meg a `keystore.txt` fájlt és **töröld ki** az első és utolsó sort (`-----BEGIN CERTIFICATE-----` és `-----END CERTIFICATE-----`), csak a középső Base64 stringet tartsd meg.

### Linux/Mac (Terminal):

```bash
base64 glideime-release.jks > keystore.txt
```

### Eredmény:

Létrejött egy `keystore.txt` fájl, ami egy hosszú Base64 string-et tartalmaz. Nyisd meg és **másold ki** az egész tartalmat.

## 3. lépés: GitHub Secrets beállítása

Most fel kell töltened a keystore-t és a jelszavakat a GitHub repository-dba.

### 3.1. Menj a GitHub repository-dba:

https://github.com/Szechko1/GlideIME-Hungarian

### 3.2. Settings → Secrets and variables → Actions

1. Kattints a repository jobb felső sarkában a **Settings** fülre
2. Bal oldali menüben: **Secrets and variables** → **Actions**
3. Kattints a **New repository secret** gombra

### 3.3. Hozd létre a következő 4 secret-et:

#### Secret 1: `KEYSTORE_FILE`
- **Name**: `KEYSTORE_FILE`
- **Value**: A `keystore.txt` fájl teljes tartalma (a hosszú Base64 string)
- Kattints **Add secret**

#### Secret 2: `KEYSTORE_PASSWORD`
- **Name**: `KEYSTORE_PASSWORD`
- **Value**: A keystore jelszó amit az 1. lépésben megadtál (pl. `MySecurePassword123`)
- Kattints **Add secret**

#### Secret 3: `KEY_ALIAS`
- **Name**: `KEY_ALIAS`
- **Value**: `glideime` (az alias amit az 1. lépésben használtál)
- Kattints **Add secret**

#### Secret 4: `KEY_PASSWORD`
- **Name**: `KEY_PASSWORD`
- **Value**: A key jelszó (általában ugyanaz mint a keystore password)
- Kattints **Add secret**

### 3.4. Ellenőrzés:

A Secrets listában most láthatod mind a 4 secret-et:
- ✅ KEYSTORE_FILE
- ✅ KEYSTORE_PASSWORD
- ✅ KEY_ALIAS
- ✅ KEY_PASSWORD

## 4. lépés: Kész!

A GitHub Actions workflow már be van állítva a secrets használatára. A következő push után automatikusan aláírt Release APK-t fog generálni!

## 5. lépés: Első aláírt APK generálása

### Manuális triggerelés:

1. Menj a GitHub repository-ba
2. Kattints az **Actions** fülre
3. Bal oldalon válaszd ki: **Android Build**
4. Jobb oldalon kattints: **Run workflow** → **Run workflow**
5. Várj 5-10 percet
6. Töltsd le a `glide-ime-release-signed` artifact-ot
7. Telepítsd a táblagépedre! ✅

### Vagy tegyél egy git push-ot:

```bash
git push
```

És automatikusan elindul a build!

## 🔒 Biztonság:

- ✅ A keystore **titkosítva** van Base64-ben
- ✅ A GitHub Secrets **titkosítva** vannak
- ✅ Senki nem látja a jelszavakat (még te sem a beállítás után)
- ✅ Csak a GitHub Actions fér hozzá

## ⚠️ Fontos megjegyzések:

1. **NE commitold** a `.jks` fájlt a repository-ba!
2. **NE oszd meg** a jelszavakat senkivel!
3. **Ments el biztonságosan** a keystore fájlt és jelszavakat (ha elveszíted, nem tudsz app frissítést kiadni)
4. A keystore **10000 napig** érvényes (~27 év)

## 🐛 Hibaelhárítás:

### "Keystore was tampered with, or password was incorrect"
- Rossz KEYSTORE_PASSWORD vagy KEY_PASSWORD → Ellenőrizd a GitHub Secrets-et

### "Could not find or load main class"
- Java/keytool nincs telepítve → Telepítsd a JDK-t

### "Base64 decode failed"
- Hibás Base64 string → Generáld újra a keystore.txt-t

---

**Készítette**: Claude Code
**Utolsó frissítés**: 2025. október
