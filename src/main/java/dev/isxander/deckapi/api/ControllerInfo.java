package dev.isxander.deckapi.api;

public record ControllerInfo(
        String strName,
        ControllerType eControllerType,
        int nXInputIndex,
        int nControllerIndex,
        int eRumblePreference,
        boolean bWireless,
        int unUniqueID,
        int unVendorID,
        int unProductID,
        int unCapabilities,
        String strFirmwareBuildTime,
        String strSerialNumber,
        String strChipID,
        short nLEDColorR, // unsigned byte
        short nLEDColorG, // unsigned byte
        short nLEDColorB, // unsigned byte
        float flLEDBrightness,
        float flLEDSaturation,
        int nTurnOnSound,
        int nTurnOffSound,
        int nLStickDeadzone,
        int nRStickDeadzone,
        int nLHapticStrength,
        int nRHapticStrength,
        float flLPadPressureCurve,
        float flRPadPressureCurve,
        boolean bHaptics,
        boolean bSWAntiDrift,
        float flGyroStationaryTolerance,
        float flAccelerometerStationaryTolerance,
        boolean bRemoteDevice,
        boolean bNintendoLayout,
        boolean bUseReveredLayout
) {}
