# steamdeck4j

A Java library for getting the Steam Deck's raw input data by using the SteamClient API
via the CEF debugger.

## Usage

```java
SteamDeck deck = SteamDeck.create(); // you can also pass in a custom CEF url here

// poll the Steam Deck for input data and join the future
// this is a manual process and will not be polled automatically
deck.poll().join(); 

ControllerState state = deck.getControllerState();
float gyroX = state.flGyroDegreesPerSecondX();
boolean r5Button = state.getButtonState(ControllerButton.R5); // back grip button

deck.close(); // unregisters listeners and closes the connection
```

## Things to note

This library requires the CEF debugger to be exposed, which it is not by default.
[Decky Loader](https://decky.xyz) is a common app used on Steam Deck to add plugins, and this
enables the CEF debugger to function. It is recommended that users install Decky as it's the easiest way
to get the CEF debugger working.