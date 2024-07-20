package dev.isxander.deckapi.api;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum ControllerType {
    NONE(-1),
    UNKNOWN(0),
    UNKNOWN_STEAM_CONTROLLER(1),
    STEAM_CONTROLLER(2),
    STEAM_CONTROLLER_V2(3),
    STEAM_DECK(4),
    FRONT_PANEL_BOARD(20),
    GENERIC(30),
    XBOX_360_CONTROLLER(31),
    XBOX_ONE_CONTROLLER(32),
    PS3_CONTROLLER(33),
    PS4_CONTROLLER(34),
    WII_CONTROLLER(35),
    APPLE_CONTROLLER(36),
    ANDROID_CONTROLLER(37),
    SWITCH_PRO_CONTROLLER(38),
    SWITCH_JOYCON_LEFT(39),
    SWITCH_JOYCON_RIGHT(40),
    SWITCH_JOYCON_PAIR(41),
    SWITCH_PRO_GENERIC_INPUT_ONLY_CONTROLLER(42),
    MOBILE_TOUCH(43),
    SWITCH_PRO_XINPUT_SWITCH_CONTROLLER(44),
    PS5_CONTROLLER(45),
    XBOX_ELITE_CONTROLLER(46),
    LAST_CONTROLLER(47),
    GENERIC_KEYBOARD(400),
    GENERIC_MOUSE(800);

    private final int id;

    ControllerType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ControllerType byId(int id) {
        return EnumSet.allOf(ControllerType.class).stream()
                .filter(controllerType -> controllerType.id() == id)
                .findFirst()
                .orElse(UNKNOWN);
    }
}
