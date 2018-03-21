import com.filter.textcorrector.text_preproccessing.TextPreproccessor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TextProccessorTest {

    private TextPreproccessor textPreproccessor;

    @Before
    public void setUp(){
        textPreproccessor = new TextPreproccessor();
    }

    @Test
    public void shouldFixSymbolsInWords() {
        assertEquals("Watch you're words! SpEll-check may not sEa words that are miss used because they are spe'lled rite!",
                textPreproccessor.preproccess("W@tch you’re w0rds! Sp3ll-check may not s3a words that are miss used bec;ause they are spe'lled r1te!"));

        assertNotEquals("Watch youre words! SpEllcheck may not sEa words that are miss used because they are spelled rite!",
                textPreproccessor.preproccess("W@tch you’re w0rds! Sp3ll-check may not s3a words that are miss used bec;ause they are spelled r1te!"));

        assertEquals("David and I satt in the corner of htthe room",
                textPreproccessor.preproccess("Dav1d and I sa/tt in the c0rn=er o'f ht+he r00m"));

        assertNotEquals("David and I satt in the corner o'f htthe room",
                textPreproccessor.preproccess("Dav1d and I sa/tt in the c0rn=er o'f ht+he r00m"));
    }

    @Test
    public void shouldFixSimilarSymbols() {
        assertEquals("3 Bithces",
                textPreproccessor.preproccess("3 Ҕithces"));

        assertEquals("bus 0 lol",
                textPreproccessor.preproccess("6us ₀ l₀l"));

        assertNotEquals("bus o lol",
                textPreproccessor.preproccess("6us ₀ l₀l"));

        assertEquals("Youare douchE and that is so sad.",
                textPreproccessor.preproccess("You#are d0uch3 and tha*t is $o sad."));
    }

    @Test
    public void shouldFixUnvantedSymbols() {
        assertEquals("My hobbys are shitting damn niggas, sometimes I hear music in the radio and their fu_c_king scream",
                textPreproccessor.preproccess("My hobbys are shitting damn n i g g a s, sometimes I hear mиsic !n the radio and their fu_c_king scream"));

        assertNotEquals("My hobbys are shitting damn niggas, sometimes I hear music in the radio and their fucking scream",
                textPreproccessor.preproccess("My hobbys are shitting damn niggas, sometimes I hear mиsic !n the radio and their fu_c_king scream"));

        assertEquals("fuck99",
                textPreproccessor.preproccess("f-u-c-k99"));

        assertEquals("I do nat likke John, He is sucha jErk!",
                textPreproccessor.preproccess("I do nat likke John, He is s_u_c_h a j3rk!"));

        assertEquals("youre so full of shit, Nealon. stop pissing me off!",
                textPreproccessor.preproccess("you*re so full of shit, Nealon. stop piss.ing me off!"));
    }

    @Test
    public void shouldFixDigits() {
        assertEquals("helloo 99 guys", textPreproccessor.preproccess("hello0 9'9 guys"));
        assertEquals("im for the 23", textPreproccessor.preproccess("i'm for the 2;Ӡ"));
        assertNotEquals("fuck03", textPreproccessor.preproccess("fuck03"));
        assertEquals("fuckoE", textPreproccessor.preproccess("fuck03"));
    }
}
