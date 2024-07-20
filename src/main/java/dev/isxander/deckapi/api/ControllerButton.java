package dev.isxander.deckapi.api;

public enum ControllerButton {
    R2(0, false),
    L2(1, false),
    R1(2, false),
    L1(3, false),
    Y(4, false),
    B(5, false),
    X(6, false),
    A(7, false),
    DPAD_UP(8, false),
    DPAD_RIGHT(9, false),
    DPAD_LEFT(10, false),
    DPAD_DOWN(11, false),
    SELECT(12, false),
    STEAM_HOME(13, false),
    START(14, false),
    L5(15, false),
    R5(16, false),
    LEFT_TOUCHPAD_CLICK(17, false),
    RIGHT_TOUCHPAD_CLICK(18, false),
    LEFT_TOUCHPAD_TOUCH(19, false),
    RIGHT_TOUCHPAD_TOUCH(20, false),
    L3(22, false),
    R3(26, false),
    MUTE(29, false), // dualsense
    L4(9, true),
    R4(10, true),
    LEFT_JOYSTICK_TOUCH(14, true),
    RIGHT_JOYSTICK_TOUCH(15, true),
    QUICK_ACCESS_MENU(18, true);

    final int bitmask;
    final boolean isUpper;
    ControllerButton(int bitpos, boolean isUpper) {
        this.bitmask = 1 << bitpos;
        this.isUpper = isUpper;
    }
}
