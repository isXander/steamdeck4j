package dev.isxander.deckapi.api;

public record ControllerState(
        int unControllerIndex,
        int unPacketNum,
        /*
         Bitmask representing pressed upper buttons.
         - Bit 0-8: Unknown (@todo Please provide more details if known)
         - Bit 9: L4
         - Bit 10: R4
         - Bit 11-13: Unknown (@todo Please provide more details if known)
         - Bit 14: Left Joystick Touch
         - Bit 15: Right Joystick Touch
         - Bit 16-17: Unknown (@todo Please provide more details if known)
         - Bit 18: Quick Access Menu
         */
        int ulUpperButtons,
        /*
         Bitmask representing pressed buttons.
         - Bit 0: R2
         - Bit 1: L2
         - Bit 2: R1
         - Bit 3: L1
         - Bit 4: Y
         - Bit 5: B
         - Bit 6: X
         - Bit 7: A
         - Bit 8: D-Pad Up
         - Bit 9: D-Pad Right
         - Bit 10: D-Pad Left
         - Bit 11: D-Pad Down
         - Bit 12: Select
         - Bit 13: Steam/Home
         - Bit 14: Start
         - Bit 15: L5
         - Bit 16: R5
         - Bit 17: Left Touchpad Click
         - Bit 18: Right Touchpad Click
         - Bit 19: Left Touchpad Touch
         - Bit 20: Right Touchpad Touch
         - Bit 21: Unknown (@todo Please provide more details if known)
         - Bit 22: L3
         - Bit 23-25: Unknown (@todo Please provide more details if known)
         - Bit 26: R3
         - Bit 27-28: Unknown (@todo Please provide more details if known)
         - Bit 29: Mute (Dualsense)
         - Bit 30-31: Unknown (@todo Please provide more details if known)
         */
        int ulButtons,
        short sLeftPadX,
        short sLeftPadY,
        short sRightPadX,
        short sRightPadY,
        short sCenterPadX,
        short sCenterPadY,
        short sLeftStickX,
        short sLeftStickY,
        short sRightStickX,
        short sRightStickY,
        short sTriggerL,
        short sTriggerR,
        float flDriftCorrectedQuatW,
        float flDriftCorrectedQuatX,
        float flDriftCorrectedQuatY,
        float flDriftCorrectedQuatZ,
        float flSensorFusionGyroQuatW,
        float flSensorFusionGyroQuatX,
        float flSensorFusionGyroQuatY,
        float flSensorFusionGyroQuatZ,
        float flDeferredSensorFusionGyroQuatW,
        float flDeferredSensorFusionGyroQuatX,
        float flDeferredSensorFusionGyroQuatY,
        float flDeferredSensorFusionGyroQuatZ,
        float flGyroDegreesPerSecondX,
        float flGyroDegreesPerSecondY,
        float flGyroDegreesPerSecondZ,
        float flGravityVectorX,
        float flGravityVectorY,
        float flGravityVectorZ,
        float flAccelerometerNoiseLength,
        float flGyroNoiseLength,
        float flGyroCalibrationProgress,
        short sBatteryLevel,
        short sPressurePadLeft,
        short sPressurePadRight,
        short sPressureBumperLeft,
        short sPressureBumperRight,
        int unHardwareUpdateInMicrosec
) {
    public boolean getButtonState(ControllerButton button) {
        return ((button.isUpper ? ulUpperButtons : ulButtons) & button.bitmask) != 0;
    }

    public String getButtonsString() {
        StringBuilder builder = new StringBuilder();
        for (ControllerButton button : ControllerButton.values()) {
            boolean state = getButtonState(button);
            builder.append(button.name()).append(": ");
            builder.append(state ? "1" : "0");
            builder.append(", ");
        }
        return builder.toString();
    }

    public static ControllerState ZERO = new ControllerState(
            0,
            0,
            0,
            0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0,
            0, 0, 0,
            0,
            0,
            0,
            (short) 0,
            (short) 0, (short) 0,
            (short) 0, (short) 0,
            0
    );
}
