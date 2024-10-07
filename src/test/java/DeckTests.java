import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.SteamDeck;
import org.junit.jupiter.api.Test;

public class DeckTests {
    @Test
    void testDeck() throws Exception {
        SteamDeck deck = SteamDeck.create("http://192.168.0.192:8081");

        deck.poll().get();

        System.out.println(deck.isGameInFocus());

        deck.close();
    }
}
