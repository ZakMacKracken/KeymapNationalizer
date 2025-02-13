import com.dekonoplyov.StringProcessor
import com.dekonoplyov.parseKeyStroke
import com.dekonoplyov.parseReplacement
import org.junit.Test
import java.awt.event.KeyEvent
import kotlin.test.*


class ParsingTest {
    @Test
    fun parseSuccess() {
        assertNotNull(parseReplacementWrapper("a with b"))
        assertNotNull(parseReplacementWrapper(" a  with b "))
        assertNotNull(parseReplacementWrapper("A with b"))
        assertNotNull(parseReplacementWrapper("a With ;"))
        assertNotNull(parseReplacementWrapper("a with 1"))
        assertNotNull(parseReplacementWrapper("a with ъ"))
        assertNotNull(parseReplacementWrapper("a with shift b"))
        assertNotNull(parseReplacementWrapper("a with alt b"))
        assertNotNull(parseReplacementWrapper("a with Meta b"))
        assertNotNull(parseReplacementWrapper("a with CTRL b"))
        assertNotNull(parseReplacementWrapper("a with ctrl  b"))
    }

    /**
     * Intellij is special with expecting uppercase key codes for the german mutated vowels (umlauts)
     * ä and ö in respect to keyboard shortcuts. The lowercase key codes are not detected in keyboard shortcuts.
     * So we have to switch the lowercase keycodes with the uppercase ones.
     * @see com.dekonoplyov.switchGermanSpecials
     */
    @Test
    fun parseGermanSpecials(){
        println("; with ö")
        val replacementOe = parseReplacementWrapper("; with ö")
        val (intOe, keyStrokeOe) = replacementOe
        println("${Integer.toHexString(intOe)} to ${Integer.toHexString(keyStrokeOe.keyCode)}")
        assertEquals(0x10000d6, keyStrokeOe.keyCode)

        println("' with ä")
        val replacementAe = parseReplacementWrapper("' with ä")
        val (intAe, keyStrokeAe) = replacementAe
        println("${Integer.toHexString(intAe)} to ${Integer.toHexString(keyStrokeAe.keyCode)}")
        assertEquals(0x10000c4, keyStrokeAe.keyCode)
    }

    @Test
    fun parseFail() {
        assertFailsWith<RuntimeException> { parseReplacementWrapper("") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("ab") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("Non with b") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with Non") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper(" with b") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with ") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with ") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with b sideli na trube") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with ctrl  ") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with ctrl some b") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("字 with b") }
        assertFailsWith<RuntimeException> { parseReplacementWrapper("a with 字") }
    }

    @Test
    fun parseSimple() {
        val replacement = parseReplacementWrapper("A with b")
        assertNotNull(replacement)
        assertEquals(replacement.first, KeyEvent.VK_A)
        val stroke = replacement.second
        assertEquals(stroke.keyCode, KeyEvent.VK_B)
        assertEquals(stroke.modifiers, 0)
    }

    @Test
    fun parseMods() {
        val mods = mergePowerSet(
            powerset(
                listOf(
                    Pair(KeyEvent.SHIFT_DOWN_MASK, "shift"),
                    Pair(KeyEvent.ALT_DOWN_MASK, "alt"),
                    Pair(KeyEvent.META_DOWN_MASK, "meta"),
                    Pair(KeyEvent.CTRL_DOWN_MASK, "ctrl")
                )
            )
        )

        for (modsToStringMods in mods) {
            val str = modsToStringMods.second + " b"
            val stroke = parseKeyStrokeWrapper(str)
            assertNotNull(stroke)
            if (modsToStringMods.first == 0) {
                assertEquals(stroke.modifiers, 0)
            } else {
                assert(modsToStringMods.first and stroke.modifiers != 0)
            }
        }
    }

    private fun parseReplacementWrapper(s: String) = parseReplacement(StringProcessor.process(s))
    private fun parseKeyStrokeWrapper(s: String) = parseKeyStroke(StringProcessor.process(s))

    private fun mergePowerSet(ps: Set<Set<Pair<Int, String>>>): List<Pair<Int, String>> {
        val l = mutableListOf<Pair<Int, String>>()
        for (set in ps) {
            var mods = 0
            var stringMods = ""
            for (modToStringMod in set) {
                mods = mods or modToStringMod.first
                stringMods += modToStringMod.second + " "
            }
            l.add(Pair(mods, stringMods))
        }
        return l
    }

    private fun <T> powerset(c: Collection<T>): Set<Set<T>> = when {
        c.isEmpty() -> setOf(setOf())
        else -> powerset(c.drop(1)).let { it -> it + it.map { it + c.first() } }
    }
}
