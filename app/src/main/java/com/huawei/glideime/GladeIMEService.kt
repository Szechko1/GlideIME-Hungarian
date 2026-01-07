package com.huawei.glideime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class GlideIMEService : InputMethodService() {

    private var shiftPressed = false
    private var altPressed = false
    private var ctrlPressed = false
    private var capsLockOn = false
    private var currentEditorInfo: EditorInfo? = null
    private var isAlternativeLayout = false // Két billentyűzet közötti váltás

    // Huawei Glide magyar billentyűzet-kiosztás #1 (Eredeti PDF alapján)
    // KeyMapping: Base, Shift, CapsLock, Alt, Shift+Alt
    private val keyboardMapOriginal = mapOf(
        // Számok (scancode 2-11)
        KeyEvent.KEYCODE_1 to KeyMapping("1", "'", "1", "1", "~"),
        KeyEvent.KEYCODE_2 to KeyMapping("2", "\"", "2", "2", "ˇ"),
        KeyEvent.KEYCODE_3 to KeyMapping("3", "+", "3", "3", "^"),
        KeyEvent.KEYCODE_4 to KeyMapping("4", "!", "4", "4", "˘"),
        KeyEvent.KEYCODE_5 to KeyMapping("5", "%", "5", "5", "°"),
        KeyEvent.KEYCODE_6 to KeyMapping("6", "/", "6", "6", "˛"),
        KeyEvent.KEYCODE_7 to KeyMapping("7", "=", "7", "7", "`"),
        KeyEvent.KEYCODE_8 to KeyMapping("8", "(", "8", "8", "˙"),
        KeyEvent.KEYCODE_9 to KeyMapping("9", ")", "9", "9", "´"),
        KeyEvent.KEYCODE_0 to KeyMapping("0", "§", "0", "0", "˝"),

        // Scancode 12-13: Ü és Ó
        KeyEvent.KEYCODE_MINUS to KeyMapping("ü", "Ü", "Ü", "ü", "¨"),
        KeyEvent.KEYCODE_EQUALS to KeyMapping("ó", "Ó", "Ó", "ó", "¸"),

        // QWERTY sor (scancode 16-27)
        KeyEvent.KEYCODE_Q to KeyMapping("q", "Q", "Q", "\\", "\\"),
        KeyEvent.KEYCODE_W to KeyMapping("w", "W", "W", "|", "|"),
        KeyEvent.KEYCODE_E to KeyMapping("e", "E", "E", "Ä", "Ä"),
        KeyEvent.KEYCODE_R to KeyMapping("r", "R", "R", "", ""),
        KeyEvent.KEYCODE_T to KeyMapping("t", "T", "T", "", ""),
        KeyEvent.KEYCODE_Y to KeyMapping("z", "Z", "Z", "", ""), // Scancode 21: Z
        KeyEvent.KEYCODE_U to KeyMapping("u", "U", "U", "€", "€"),
        KeyEvent.KEYCODE_I to KeyMapping("i", "I", "I", "í", "Í"),
        KeyEvent.KEYCODE_O to KeyMapping("o", "O", "O", "ö", "Ö"), // Shift+Alt+O → Ö
        KeyEvent.KEYCODE_P to KeyMapping("p", "P", "P", "", ""),

        // Scancode 26-27: Ő és Ú
        KeyEvent.KEYCODE_LEFT_BRACKET to KeyMapping("ő", "Ő", "Ő", "÷", "÷"),
        KeyEvent.KEYCODE_RIGHT_BRACKET to KeyMapping("ú", "Ú", "Ú", "×", "×"),

        // ASDFGH sor (scancode 30-40)
        KeyEvent.KEYCODE_A to KeyMapping("a", "A", "A", "ä", "ä"),
        KeyEvent.KEYCODE_S to KeyMapping("s", "S", "S", "đ", "đ"),
        KeyEvent.KEYCODE_D to KeyMapping("d", "D", "D", "Đ", "Đ"),
        KeyEvent.KEYCODE_F to KeyMapping("f", "F", "F", "[", "["),
        KeyEvent.KEYCODE_G to KeyMapping("g", "G", "G", "]", "]"),
        KeyEvent.KEYCODE_H to KeyMapping("h", "H", "H", "", ""),
        KeyEvent.KEYCODE_J to KeyMapping("j", "J", "J", "í", "í"),
        KeyEvent.KEYCODE_K to KeyMapping("k", "K", "K", "ł", "ł"),
        KeyEvent.KEYCODE_L to KeyMapping("l", "L", "L", "Ł", "Ł"),

        // Scancode 39-40: É és Á
        KeyEvent.KEYCODE_SEMICOLON to KeyMapping("é", "É", "É", "$", "$"),
        KeyEvent.KEYCODE_APOSTROPHE to KeyMapping("á", "Á", "Á", "ß", "ß"),

        // Scancode 43: Ű (az Enter előtt)
        KeyEvent.KEYCODE_BACKSLASH to KeyMapping("ű", "Ű", "Ű", "¤", "¤"),

        // YXCVBN sor (scancode 44-53)
        KeyEvent.KEYCODE_Z to KeyMapping("y", "Y", "Y", ">", ">"), // Scancode 44: Y
        KeyEvent.KEYCODE_X to KeyMapping("x", "X", "X", "#", "#"),
        KeyEvent.KEYCODE_C to KeyMapping("c", "C", "C", "&", "&"),
        KeyEvent.KEYCODE_V to KeyMapping("v", "V", "V", "@", "@"),
        KeyEvent.KEYCODE_B to KeyMapping("b", "B", "B", "{", "{"),
        KeyEvent.KEYCODE_N to KeyMapping("n", "N", "N", "}", "}"),
        KeyEvent.KEYCODE_M to KeyMapping("m", "M", "M", "µ", "µ"),

        // Scancode 51-53: pontozás
        KeyEvent.KEYCODE_COMMA to KeyMapping(",", "?", "?", ";", ";"),
        KeyEvent.KEYCODE_PERIOD to KeyMapping(".", ":", ":", "<", "<"),
        KeyEvent.KEYCODE_SLASH to KeyMapping("-", "_", "_", "*", "*"),

        // Spacebar
        KeyEvent.KEYCODE_SPACE to KeyMapping(" ", " ", " ", " ", " ")
    )

    // Huawei Glide magyar billentyűzet-kiosztás #2 (Módosított verzió)
    // Esc → 0, 0 → ö/Ö, Shift+0 → Ö
    private val keyboardMapAlternative = mapOf(
        // Számok (scancode 2-11) - MÓDOSÍTOTT
        KeyEvent.KEYCODE_1 to KeyMapping("1", "'", "1", "1", "~"),
        KeyEvent.KEYCODE_2 to KeyMapping("2", "\"", "2", "2", "ˇ"),
        KeyEvent.KEYCODE_3 to KeyMapping("3", "+", "3", "3", "^"),
        KeyEvent.KEYCODE_4 to KeyMapping("4", "!", "4", "4", "˘"),
        KeyEvent.KEYCODE_5 to KeyMapping("5", "%", "5", "5", "°"),
        KeyEvent.KEYCODE_6 to KeyMapping("6", "/", "6", "6", "˛"),
        KeyEvent.KEYCODE_7 to KeyMapping("7", "=", "7", "7", "`"),
        KeyEvent.KEYCODE_8 to KeyMapping("8", "(", "8", "8", "˙"),
        KeyEvent.KEYCODE_9 to KeyMapping("9", ")", "9", "9", "´"),
        KeyEvent.KEYCODE_0 to KeyMapping("ö", "Ö", "Ö", "ö", "Ö"), // MÓDOSÍTOTT: 0 → ö/Ö

        // Scancode 12-13: Ü és Ó
        KeyEvent.KEYCODE_MINUS to KeyMapping("ü", "Ü", "Ü", "ü", "¨"),
        KeyEvent.KEYCODE_EQUALS to KeyMapping("ó", "Ó", "Ó", "ó", "¸"),

        // QWERTY sor (scancode 16-27)
        KeyEvent.KEYCODE_Q to KeyMapping("q", "Q", "Q", "\\", "\\"),
        KeyEvent.KEYCODE_W to KeyMapping("w", "W", "W", "|", "|"),
        KeyEvent.KEYCODE_E to KeyMapping("e", "E", "E", "Ä", "Ä"),
        KeyEvent.KEYCODE_R to KeyMapping("r", "R", "R", "", ""),
        KeyEvent.KEYCODE_T to KeyMapping("t", "T", "T", "", ""),
        KeyEvent.KEYCODE_Y to KeyMapping("z", "Z", "Z", "", ""), // Scancode 21: Z
        KeyEvent.KEYCODE_U to KeyMapping("u", "U", "U", "€", "€"),
        KeyEvent.KEYCODE_I to KeyMapping("i", "I", "I", "í", "Í"),
        KeyEvent.KEYCODE_O to KeyMapping("o", "O", "O", "ö", "Ö"),
        KeyEvent.KEYCODE_P to KeyMapping("p", "P", "P", "", ""),

        // Scancode 26-27: Ő és Ú
        KeyEvent.KEYCODE_LEFT_BRACKET to KeyMapping("ő", "Ő", "Ő", "÷", "÷"),
        KeyEvent.KEYCODE_RIGHT_BRACKET to KeyMapping("ú", "Ú", "Ú", "×", "×"),

        // ASDFGH sor (scancode 30-40)
        KeyEvent.KEYCODE_A to KeyMapping("a", "A", "A", "ä", "ä"),
        KeyEvent.KEYCODE_S to KeyMapping("s", "S", "S", "đ", "đ"),
        KeyEvent.KEYCODE_D to KeyMapping("d", "D", "D", "Đ", "Đ"),
        KeyEvent.KEYCODE_F to KeyMapping("f", "F", "F", "[", "["),
        KeyEvent.KEYCODE_G to KeyMapping("g", "G", "G", "]", "]"),
        KeyEvent.KEYCODE_H to KeyMapping("h", "H", "H", "", ""),
        KeyEvent.KEYCODE_J to KeyMapping("j", "J", "J", "í", "í"),
        KeyEvent.KEYCODE_K to KeyMapping("k", "K", "K", "ł", "ł"),
        KeyEvent.KEYCODE_L to KeyMapping("l", "L", "L", "Ł", "Ł"),

        // Scancode 39-40: É és Á
        KeyEvent.KEYCODE_SEMICOLON to KeyMapping("é", "É", "É", "$", "$"),
        KeyEvent.KEYCODE_APOSTROPHE to KeyMapping("á", "Á", "Á", "ß", "ß"),

        // Scancode 43: Ű (az Enter előtt)
        KeyEvent.KEYCODE_BACKSLASH to KeyMapping("ű", "Ű", "Ű", "¤", "¤"),

        // YXCVBN sor (scancode 44-53)
        KeyEvent.KEYCODE_Z to KeyMapping("y", "Y", "Y", ">", ">"), // Scancode 44: Y
        KeyEvent.KEYCODE_X to KeyMapping("x", "X", "X", "#", "#"),
        KeyEvent.KEYCODE_C to KeyMapping("c", "C", "C", "&", "&"),
        KeyEvent.KEYCODE_V to KeyMapping("v", "V", "V", "@", "@"),
        KeyEvent.KEYCODE_B to KeyMapping("b", "B", "B", "{", "{"),
        KeyEvent.KEYCODE_N to KeyMapping("n", "N", "N", "}", "}"),
        KeyEvent.KEYCODE_M to KeyMapping("m", "M", "M", "µ", "µ"),

        // Scancode 51-53: pontozás
        KeyEvent.KEYCODE_COMMA to KeyMapping(",", "?", "?", ";", ";"),
        KeyEvent.KEYCODE_PERIOD to KeyMapping(".", ":", ":", "<", "<"),
        KeyEvent.KEYCODE_SLASH to KeyMapping("-", "_", "_", "*", "*"),

        // Spacebar
        KeyEvent.KEYCODE_SPACE to KeyMapping(" ", " ", " ", " ", " "),

        // Escape → 0 (MÓDOSÍTOTT)
        KeyEvent.KEYCODE_ESCAPE to KeyMapping("0", "0", "0", "0", "0")
    )

    // Aktuális billentyűzet kiosztás lekérése
    private fun getCurrentKeyboardMap(): Map<Int, KeyMapping> {
        return if (isAlternativeLayout) keyboardMapAlternative else keyboardMapOriginal
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEditorInfo = info
        shiftPressed = false
        altPressed = false
        ctrlPressed = false
        capsLockOn = false
        // Ne reseteljük az isAlternativeLayout-ot, hogy megmaradjon a választott kiosztás
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyDown(keyCode, event)

        try {
            // ==================== MÓDOSÍTÓ BILLENTYŰK ====================

            // Ctrl billentyű kezelése
            if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
                ctrlPressed = true
                return true
            }

            // Shift kezelése
            if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                shiftPressed = true
                return true
            }

            // Alt kezelése
            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                altPressed = true
                return true
            }

            // Caps Lock kezelése
            if (keyCode == KeyEvent.KEYCODE_CAPS_LOCK) {
                capsLockOn = !capsLockOn
                return true
            }

            // ==================== CTRL KOMBINÁCIÓK ====================

            if (ctrlPressed) {
                when (keyCode) {
                    // Ctrl+C - Copy (Másolás)
                    KeyEvent.KEYCODE_C -> {
                        performCopy()
                        return true
                    }

                    // Ctrl+X - Cut (Kivágás)
                    KeyEvent.KEYCODE_X -> {
                        performCut()
                        return true
                    }

                    // Ctrl+V - Paste (Beillesztés)
                    KeyEvent.KEYCODE_V -> {
                        performPaste()
                        return true
                    }

                    // Ctrl+A - Select All (Mindet kijelöli)
                    KeyEvent.KEYCODE_A -> {
                        performSelectAll()
                        return true
                    }

                    // Ctrl+Z - Undo (Visszavonás)
                    KeyEvent.KEYCODE_Z -> {
                        performUndo()
                        return true
                    }

                    // Ctrl+Y - Redo (Mégis)
                    KeyEvent.KEYCODE_Y -> {
                        performRedo()
                        return true
                    }

                    // Ctrl+S - Save (Mentés)
                    KeyEvent.KEYCODE_S -> {
                        sendCtrlKey(KeyEvent.KEYCODE_S)
                        return true
                    }

                    // Ctrl+F - Find (Keresés)
                    KeyEvent.KEYCODE_F -> {
                        sendCtrlKey(KeyEvent.KEYCODE_F)
                        return true
                    }

                    // Ctrl+H - Replace (Csere)
                    KeyEvent.KEYCODE_H -> {
                        sendCtrlKey(KeyEvent.KEYCODE_H)
                        return true
                    }

                    // Ctrl+N - New (Új)
                    KeyEvent.KEYCODE_N -> {
                        sendCtrlKey(KeyEvent.KEYCODE_N)
                        return true
                    }

                    // Ctrl+O - Open (Megnyitás)
                    KeyEvent.KEYCODE_O -> {
                        sendCtrlKey(KeyEvent.KEYCODE_O)
                        return true
                    }

                    // Ctrl+P - Print (Nyomtatás)
                    KeyEvent.KEYCODE_P -> {
                        sendCtrlKey(KeyEvent.KEYCODE_P)
                        return true
                    }

                    // Ctrl+W - Close (Bezárás)
                    KeyEvent.KEYCODE_W -> {
                        sendCtrlKey(KeyEvent.KEYCODE_W)
                        return true
                    }

                    // Ctrl+Q - Quit (Kilépés)
                    KeyEvent.KEYCODE_Q -> {
                        sendCtrlKey(KeyEvent.KEYCODE_Q)
                        return true
                    }

                    // Ctrl+T - New Tab (Új lap)
                    KeyEvent.KEYCODE_T -> {
                        sendCtrlKey(KeyEvent.KEYCODE_T)
                        return true
                    }

                    // Ctrl+R - Refresh/Reload (Frissítés)
                    KeyEvent.KEYCODE_R -> {
                        sendCtrlKey(KeyEvent.KEYCODE_R)
                        return true
                    }

                    // Ctrl+B - Bold (Félkövér)
                    KeyEvent.KEYCODE_B -> {
                        sendCtrlKey(KeyEvent.KEYCODE_B)
                        return true
                    }

                    // Ctrl+I - Italic (Dőlt)
                    KeyEvent.KEYCODE_I -> {
                        sendCtrlKey(KeyEvent.KEYCODE_I)
                        return true
                    }

                    // Ctrl+U - Underline (Aláhúzott)
                    KeyEvent.KEYCODE_U -> {
                        sendCtrlKey(KeyEvent.KEYCODE_U)
                        return true
                    }

                    // Ctrl+K - Insert Link (Link beszúrása)
                    KeyEvent.KEYCODE_K -> {
                        sendCtrlKey(KeyEvent.KEYCODE_K)
                        return true
                    }

                    // Ctrl+L - Select Address Bar (Címsor kijelölése)
                    KeyEvent.KEYCODE_L -> {
                        sendCtrlKey(KeyEvent.KEYCODE_L)
                        return true
                    }

                    // Ctrl+D - Bookmark (Könyvjelző)
                    KeyEvent.KEYCODE_D -> {
                        sendCtrlKey(KeyEvent.KEYCODE_D)
                        return true
                    }

                    // Ctrl+G - Go to line (Ugrás sorra)
                    KeyEvent.KEYCODE_G -> {
                        sendCtrlKey(KeyEvent.KEYCODE_G)
                        return true
                    }

                    // Ctrl+E - Search (Keresés)
                    KeyEvent.KEYCODE_E -> {
                        sendCtrlKey(KeyEvent.KEYCODE_E)
                        return true
                    }

                    // Ctrl+Home - Dokumentum elejére
                    KeyEvent.KEYCODE_MOVE_HOME -> {
                        performMoveToStart()
                        return true
                    }

                    // Ctrl+End - Dokumentum végére
                    KeyEvent.KEYCODE_MOVE_END -> {
                        performMoveToEnd()
                        return true
                    }

                    // Ctrl+Left - Szó balra
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        performWordLeft()
                        return true
                    }

                    // Ctrl+Right - Szó jobbra
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        performWordRight()
                        return true
                    }

                    // Ctrl+Backspace - Szó törlése balra
                    KeyEvent.KEYCODE_DEL -> {
                        performDeleteWordBackward()
                        return true
                    }

                    // Ctrl+Delete - Szó törlése jobbra
                    KeyEvent.KEYCODE_FORWARD_DEL -> {
                        performDeleteWordForward()
                        return true
                    }
                }

                // Ha Ctrl van nyomva, ne dolgozza fel a normál karaktereket
                return super.onKeyDown(keyCode, event)
            }

            // ==================== SPECIÁLIS BILLENTYŰK ====================

            // Backspace (Delete backward)
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                val ic = currentInputConnection
                if (ic != null) {
                    // Először ellenőrizzük, hogy van-e kijelölt szöveg
                    val selectedText = ic.getSelectedText(0)

                    if (selectedText != null && selectedText.isNotEmpty()) {
                        // Ha van kijelölt szöveg, töröljük egyben
                        ic.commitText("", 1)
                        return true
                    }

                    // Ha nincs kijelölt szöveg, ellenőrizzük hogy üres-e a mező
                    val textBefore = ic.getTextBeforeCursor(1, 0)
                    val textAfter = ic.getTextAfterCursor(1, 0)

                    // Ha a mező üres, visszalépünk az előző mezőre
                    if ((textBefore == null || textBefore.isEmpty()) &&
                        (textAfter == null || textAfter.isEmpty())) {
                        checkAndRetreatToPreviousField()
                    } else {
                        // Normál törlés - egy karakter
                        ic.deleteSurroundingText(1, 0)
                    }
                }
                return true
            }

            // Delete (Forward delete)
            if (keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
                currentInputConnection?.deleteSurroundingText(0, 1)
                return true
            }

            // Enter - INTELLIGENS KEZELÉS
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                return handleEnterKey()
            }

            // Tab
            if (keyCode == KeyEvent.KEYCODE_TAB) {
                currentInputConnection?.commitText("\t", 1)
                return true
            }

            // Escape - ellenőrizzük, hogy van-e egyedi mapping
            if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                val mapping = getCurrentKeyboardMap()[keyCode]
                if (mapping != null) {
                    // Ha van mapping (pl. alternatív layoutban), azt használjuk
                    val character = when {
                        altPressed && shiftPressed -> mapping.shiftAlt
                        altPressed -> mapping.alt
                        shiftPressed || capsLockOn -> mapping.shift
                        else -> mapping.base
                    }
                    if (character.isNotEmpty()) {
                        currentInputConnection?.commitText(character, 1)
                        return true
                    }
                }
                // Ha nincs mapping, normál Escape funkció
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE)
                return true
            }

            // Arrow keys (nyíl billentyűk) - Shift támogatással
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {

                // Ha Shift van nyomva, szöveg kijelölés
                if (shiftPressed) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                            // Shift+Balra = Kijelölés balra
                            performSelectLeft()
                            return true
                        }
                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            // Shift+Jobbra = Kijelölés jobbra
                            performSelectRight()
                            return true
                        }
                        KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
                            // Shift+Fel/Le - KeyEvent küldése
                            sendKeyWithShift(keyCode)
                            return true
                        }
                    }
                } else {
                    // Shift nélkül normál KeyEvent
                    sendDownUpKeyEvents(keyCode)
                }
                return true
            }

            // Home - normál használat (sor eleje)
            if (keyCode == KeyEvent.KEYCODE_MOVE_HOME) {
                if (shiftPressed) {
                    sendKeyWithShift(keyCode)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_HOME)
                }
                return true
            }

            // End - normál használat (sor vége)
            if (keyCode == KeyEvent.KEYCODE_MOVE_END) {
                if (shiftPressed) {
                    sendKeyWithShift(keyCode)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_MOVE_END)
                }
                return true
            }

            // Page Up - Shift támogatással
            if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
                if (shiftPressed) {
                    sendKeyWithShift(keyCode)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_PAGE_UP)
                }
                return true
            }

            // Page Down - Shift támogatással
            if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
                if (shiftPressed) {
                    sendKeyWithShift(keyCode)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_PAGE_DOWN)
                }
                return true
            }

            // Insert
            if (keyCode == KeyEvent.KEYCODE_INSERT) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_INSERT)
                return true
            }

            // ==================== NORMÁL KARAKTEREK ====================

            // SHIFT+SPACE - Billentyűzet váltás
            if (keyCode == KeyEvent.KEYCODE_SPACE && shiftPressed) {
                isAlternativeLayout = !isAlternativeLayout
                val layoutName = if (isAlternativeLayout) "Alternatív" else "Eredeti"
                showToast("Billentyűzet: $layoutName kiosztás")
                return true
            }

            // Billentyűzet-kiosztás kezelése
            val mapping = getCurrentKeyboardMap()[keyCode]
            if (mapping != null) {
                val character = when {
                    altPressed && shiftPressed -> mapping.shiftAlt  // Shift+Alt
                    altPressed -> mapping.alt                        // Alt
                    shiftPressed || capsLockOn -> mapping.shift      // Shift vagy Caps Lock
                    else -> mapping.base                             // Normál
                }

                if (character.isNotEmpty()) {
                    // KRITIKUS: Mentsük el az EditorInfo-t MIELŐTT commitText-et hívunk!
                    // Használjuk a beépített currentInputEditorInfo-t (nem a sajátunkat!)
                    val ic = currentInputConnection
                    val editorInfo = currentInputEditorInfo  // InputMethodService beépített property

                    ic?.commitText(character, 1)

                    // DEBUG: Ellenőrizzük hogy egyáltalán idáig eljutunk-e
                    showToast("Beírva: $character")

                    // OTP mezők automatikus továbbítása - átadjuk az elmentett értékeket
                    checkAndAdvanceToNextField(ic, editorInfo)

                    return true
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        try {
            if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT) {
                ctrlPressed = false
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
                shiftPressed = false
                return true
            }

            if (keyCode == KeyEvent.KEYCODE_ALT_LEFT || keyCode == KeyEvent.KEYCODE_ALT_RIGHT) {
                altPressed = false
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return super.onKeyUp(keyCode, event)
    }

    // ==================== ENTER BILLENTYŰ KEZELÉSE ====================

    private fun handleEnterKey(): Boolean {
        val info = currentEditorInfo
        val ic = currentInputConnection ?: return false

        // Ha nincs EditorInfo, küldjünk KeyEvent-et
        if (info == null) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            return true
        }

        val inputType = info.inputType

        // Ellenőrizzük, hogy többsoros mező-e
        val isMultiLine = (inputType and EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0

        return when {
            // Ha többsoros mező, új sort szúrunk be
            isMultiLine -> {
                ic.commitText("\n", 1)
                true
            }

            // MINDEN MÁS ESETBEN KeyEvent-et küldünk
            // (keresőmezők, URL mezők, form mezők stb.)
            else -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                true
            }
        }
    }

    // ==================== CTRL ÉS SHIFT FUNKCIÓK ====================

    // Ctrl+Billentyű küldése
    private fun sendCtrlKey(keyCode: Int) {
        try {
            val ic = currentInputConnection ?: return
            val eventTime = System.currentTimeMillis()

            val downEvent = KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0,
                KeyEvent.META_CTRL_ON
            )

            val upEvent = KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                keyCode,
                0,
                KeyEvent.META_CTRL_ON
            )

            ic.sendKeyEvent(downEvent)
            ic.sendKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Shift+Billentyű küldése
    private fun sendKeyWithShift(keyCode: Int) {
        try {
            val ic = currentInputConnection ?: return
            val eventTime = System.currentTimeMillis()

            val downEvent = KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_DOWN,
                keyCode,
                0,
                KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
            )

            val upEvent = KeyEvent(
                eventTime,
                eventTime,
                KeyEvent.ACTION_UP,
                keyCode,
                0,
                KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
            )

            ic.sendKeyEvent(downEvent)
            ic.sendKeyEvent(upEvent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Shift+Balra - Kijelölés balra
    private fun performSelectLeft() {
        sendKeyWithShift(KeyEvent.KEYCODE_DPAD_LEFT)
    }

    // Shift+Jobbra - Kijelölés jobbra
    private fun performSelectRight() {
        sendKeyWithShift(KeyEvent.KEYCODE_DPAD_RIGHT)
    }

    private fun performCopy() {
        try {
            val ic = currentInputConnection ?: return
            val selectedText = ic.getSelectedText(0)

            if (selectedText != null && selectedText.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", selectedText)
                clipboard.setPrimaryClip(clip)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performCut() {
        try {
            val ic = currentInputConnection ?: return
            val selectedText = ic.getSelectedText(0)

            if (selectedText != null && selectedText.isNotEmpty()) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Cut Text", selectedText)
                clipboard.setPrimaryClip(clip)

                ic.commitText("", 1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performPaste() {
        try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (clipboard.hasPrimaryClip()) {
                val clipData = clipboard.primaryClip
                val item = clipData?.getItemAt(0)
                val pasteText = item?.text

                if (pasteText != null && pasteText.isNotEmpty()) {
                    currentInputConnection?.commitText(pasteText, 1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performSelectAll() {
        try {
            val ic = currentInputConnection ?: return
            ic.performContextMenuAction(android.R.id.selectAll)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performUndo() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_Z)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performRedo() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_Y)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performMoveToStart() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_MOVE_HOME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performMoveToEnd() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_MOVE_END)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performWordLeft() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_DPAD_LEFT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performWordRight() {
        try {
            sendCtrlKey(KeyEvent.KEYCODE_DPAD_RIGHT)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performDeleteWordBackward() {
        try {
            val ic = currentInputConnection ?: return
            val textBeforeCursor = ic.getTextBeforeCursor(100, 0) ?: return

            val lastSpace = textBeforeCursor.lastIndexOfAny(charArrayOf(' ', '\n', '\t'))
            val deleteCount = if (lastSpace >= 0) {
                textBeforeCursor.length - lastSpace - 1
            } else {
                textBeforeCursor.length
            }

            if (deleteCount > 0) {
                ic.deleteSurroundingText(deleteCount, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performDeleteWordForward() {
        try {
            val ic = currentInputConnection ?: return
            val textAfterCursor = ic.getTextAfterCursor(100, 0) ?: return

            val nextSpace = textAfterCursor.indexOfAny(charArrayOf(' ', '\n', '\t'))
            val deleteCount = if (nextSpace >= 0) {
                nextSpace + 1
            } else {
                textAfterCursor.length
            }

            if (deleteCount > 0) {
                ic.deleteSurroundingText(0, deleteCount)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // OTP mezők automatikus továbbítása (egyszeri jelszó beviteli mezők)
    private fun checkAndAdvanceToNextField(ic: InputConnection?, info: EditorInfo?) {
        // DEBUG: függvény meghívva
        showToast("checkAndAdvance HÍVVA")

        try {
            if (ic == null) {
                showToast("inputConnection NULL!")
                return
            }

            if (info == null) {
                showToast("editorInfo NULL!")
                return
            }

            val inputType = info.inputType
            val imeOptions = info.imeOptions

            // Alapszűrés: csak NUMBER vagy TEXT mezőknél
            val isNumberType = (inputType and EditorInfo.TYPE_CLASS_NUMBER) != 0
            val isTextType = (inputType and EditorInfo.TYPE_CLASS_TEXT) != 0

            // DEBUG: mutassuk meg a mező típusát
            showToast("Típus: NUM=$isNumberType TEXT=$isTextType")

            if (!isNumberType && !isTextType) {
                showToast("Nem NUM/TEXT - kilép")
                return
            }

            // KRITIKUS: A commitText után AZONNAL hívjuk meg ezt a függvényt,
            // de a getTextBeforeCursor() még NEM látja az új karaktert!
            // Ezért MINDENT a Handler-ben kell csinálni, késleltetéssel!

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // MOST ellenőrizzük a szöveg hosszát (a commitText már befejeződött)
                    val textBeforeCursor = ic.getTextBeforeCursor(100, 0) ?: ""
                    val textAfterCursor = ic.getTextAfterCursor(100, 0) ?: ""
                    val currentTextLength = textBeforeCursor.length + textAfterCursor.length

                    // DEBUG: mutassuk meg a szöveg hosszát
                    showToast("Hossz: $currentTextLength")

                    // Ha pontosan 1 karakter van (OTP jellemző), akkor navigálunk
                    if (currentTextLength == 1) {
                        val hasNextAction = (imeOptions and EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_NEXT

                        showToast("Nav: hasNext=$hasNextAction")

                        // Többféle navigációs módszert próbálunk
                        if (hasNextAction) {
                            // Natív Android mezők: IME_ACTION_NEXT
                            ic.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                            showToast("IME_ACTION_NEXT küldve")
                        } else {
                            // Webes formok: Tab KeyEvent küldése
                            val eventTime = System.currentTimeMillis()
                            val tabDownEvent = KeyEvent(
                                eventTime,
                                eventTime,
                                KeyEvent.ACTION_DOWN,
                                KeyEvent.KEYCODE_TAB,
                                0,
                                0
                            )
                            val tabUpEvent = KeyEvent(
                                eventTime,
                                eventTime,
                                KeyEvent.ACTION_UP,
                                KeyEvent.KEYCODE_TAB,
                                0,
                                0
                            )
                            ic.sendKeyEvent(tabDownEvent)
                            ic.sendKeyEvent(tabUpEvent)
                            showToast("Tab küldve")
                        }
                    } else {
                        showToast("Nem 1 kar - nem nav")
                    }
                } catch (e: Exception) {
                    showToast("Hiba Handler: ${e.message}")
                    e.printStackTrace()
                }
            }, 200) // 200ms késleltetés - biztos, hogy commitText befejeződött
        } catch (e: Exception) {
            showToast("Hiba check: ${e.message}")
            e.printStackTrace()
        }
    }

    // OTP mezők automatikus visszalépés az előző mezőre (Backspace üres mezőben)
    private fun checkAndRetreatToPreviousField() {
        try {
            val ic = currentInputConnection ?: return
            val info = currentEditorInfo ?: return

            val inputType = info.inputType

            // Heurisztika: OTP mezők jellemzői
            val isNumberType = (inputType and EditorInfo.TYPE_CLASS_NUMBER) != 0
            val isTextType = (inputType and EditorInfo.TYPE_CLASS_TEXT) != 0

            val isLikelyOTPField = isNumberType || isTextType

            // Ha OTP-szerű mező és üres, megpróbálunk visszalépni
            if (isLikelyOTPField) {
                // Kis késleltetés
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        // Shift+Tab kombináció küldése, ami általában az előző mezőre lép
                        // Ez webes és natív formoknál is működik
                        val eventTime = System.currentTimeMillis()
                        val downEvent = KeyEvent(
                            eventTime,
                            eventTime,
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_TAB,
                            0,
                            KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
                        )
                        val upEvent = KeyEvent(
                            eventTime,
                            eventTime,
                            KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_TAB,
                            0,
                            KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
                        )
                        currentInputConnection?.sendKeyEvent(downEvent)
                        currentInputConnection?.sendKeyEvent(upEvent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, 100) // 100ms késleltetés
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // KeyMapping: Base, Shift, CapsLock, Alt, Shift+Alt
    data class KeyMapping(
        val base: String,
        val shift: String,
        val caps: String,
        val alt: String,
        val shiftAlt: String
    )
}
