import dev.isxander.deckapi.api.ControllerState;
import dev.isxander.deckapi.api.SteamDeck;
import org.junit.jupiter.api.Test;

public class DeckTests {
    @Test
    void testDeck() throws Exception {
        SteamDeck deck = SteamDeck.create("http://192.168.0.121:8081");

        while (true) {
            deck.poll().get();
            ControllerState state = deck.getControllerState();
            System.out.println(state);
            System.out.println("Pitch %s Yaw %s Roll %s".formatted(state.flSoftwareGyroDegreesPerSecondPitch(), state.flSoftwareGyroDegreesPerSecondYaw(), state.flSoftwareGyroDegreesPerSecondRoll()));
        }

//        deck.close();
    }
}
