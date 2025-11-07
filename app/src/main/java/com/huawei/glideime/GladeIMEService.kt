package com.huawei.glideime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class GlideIMEService : InputMethodService() {

    private var shiftPressed = false
    private var altPressed = false
    private var ctrlPressed = false
    private var capsLockOn = false
    private var currentEditorInfo: EditorInfo? = null
    private var currentLayout = 1 // 1 = magyar-1, 2 = magyar-2
    private var spacePressed = false // Shift+Space kombinációhoz

    // Huawei Glide magyar-1 billentyűzet-kiosztás (PDF alapján)
    // KeyMapping: Base, Shift, CapsLock, Alt, Shift+Alt
    private val keyboardMapHungarian1 = mapOf(
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
        KeyEvent.KEYCODE_C to KeyMapping("c", "C", "C", "c", "c"),
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

    // Huawei Glide magyar-2 billentyűzet-kiosztás (módosított)
    // Különbségek: ESC helyén 0, 0 helyén ö/Ö
    private val keyboardMapHungarian2 = mapOf(
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
        KeyEvent.KEYCODE_0 to KeyMapping("ö", "Ö", "Ö", "ö", "ö"), // MÓDOSÍTVA: 0 helyén ö/Ö

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
        KeyEvent.KEYCODE_C to KeyMapping("c", "C", "C", "c", "c"),
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

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentEditorInfo = info
        shiftPressed = false
        altPressed = false
        ctrlPressed = false
        capsLockOn = false
        spacePressed = false
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

            // ESC billentyű kezelése - Magyar-2 layout esetén 0, Magyar-1-ben ESC
            if (keyCode == KeyEvent.KEYCODE_ESCAPE) {
                if (currentLayout == 2) {
                    // Magyar-2 layout: ESC helyén 0
                    // NE küldünk KeyEvent-et, csak szöveg
                    currentInputConnection?.commitText("0", 1)
                } else {
                    // Magyar-1 layout: normál ESC funkció
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ESCAPE)
                }
                return true
            }

            // ==================== LAYOUT VÁLTÁS (SHIFT+SPACE) ====================

            // Space billentyű lenyomása
            if (keyCode == KeyEvent.KEYCODE_SPACE) {
                spacePressed = true

                // Ha Shift+Space, akkor layout váltás
                if (shiftPressed) {
                    currentLayout = if (currentLayout == 1) 2 else 1
                    val layoutName = if (currentLayout == 1) "Magyar-1" else "Magyar-2"
                    showToast("Billentyűzet: $layoutName")
                    return true
                }
                // Ha csak Space (Shift nélkül), akkor később kerül feldolgozásra a keyboardMap-ből
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
                currentInputConnection?.deleteSurroundingText(1, 0)
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

            // Megfelelő billentyűzet-kiosztás kiválasztása
            val keyboardMap = if (currentLayout == 1) keyboardMapHungarian1 else keyboardMapHungarian2

            // Billentyűzet-kiosztás kezelése
            val mapping = keyboardMap[keyCode]
            if (mapping != null) {
                val character = when {
                    altPressed && shiftPressed -> mapping.shiftAlt  // Shift+Alt
                    altPressed -> mapping.alt                        // Alt
                    shiftPressed || capsLockOn -> mapping.shift      // Shift vagy Caps Lock
                    else -> mapping.base                             // Normál
                }

                if (character.isNotEmpty()) {
                    currentInputConnection?.commitText(character, 1)
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

            if (keyCode == KeyEvent.KEYCODE_SPACE) {
                spacePressed = false
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