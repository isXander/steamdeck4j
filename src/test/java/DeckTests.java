import dev.isxander.deckapi.api.SteamDeck;
import org.junit.jupiter.api.Test;

public class DeckTests {
    @Test
    void testDeck() throws Exception {
        SteamDeck deck = SteamDeck.create("192.168.0.121:8081");

        deck.poll().join();
        System.out.println(deck.getControllerState());
        System.out.println(deck.getControllerInfo());

        deck.close();
    }
}
