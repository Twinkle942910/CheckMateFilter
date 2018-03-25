import com.filter.textcorrector.spellchecking.Language;
import com.filter.textcorrector.spellchecking.Spellchecker;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class SpellcheckerTest {

    private Spellchecker spellchecker;

    @Before
    public void setUp(){
        spellchecker = new Spellchecker(Language.ENGLISH);
    }

    @Test
    public void shouldCheckSpellingMistakes() {
        //assertNotEquals(spellchecker.checkText("hello, fuckink wrodl!"), "hello, fucking world!");
        //assertEquals(spellchecker.checkText("hello, fuckink wrodl!"), "Hello, fuck ink world!");

        assertEquals(spellchecker.checkText("seee you later, aaligator."), "See you later, alligator.");

        //assertNotEquals(spellchecker.checkText("helo, wrold!"), "hello, world!");
        assertEquals(spellchecker.checkText("helo, wrold!"), "Halo, world!");

        assertNotEquals(spellchecker.checkText("beginnin."), "beginning.");
        assertEquals(spellchecker.checkText("beginnin."), "Begin in.");

        assertEquals(spellchecker.checkText("intrenatioonal intitution"), "International institution");
        assertEquals(spellchecker.checkText("i can liift a car all by mayself"), "I can lift a car all by myself");
        assertEquals(spellchecker.checkText("accomodate"), "Accommodate");
        assertEquals(spellchecker.checkText("hello, darknes, my old frend!"), "Hello, darkness, my old friend!");
        assertEquals(spellchecker.checkText("belive"), "Believe");

        assertNotEquals(spellchecker.checkText("croner kase"), "Corner case");
        assertEquals(spellchecker.checkText("croner kase"), "Corner Kate");
    }
}
