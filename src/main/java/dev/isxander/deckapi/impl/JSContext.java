package dev.isxander.deckapi.impl;

import org.intellij.lang.annotations.Language;

public class JSContext {
    @Language("TypeScript")
    public static final String JS_CONTEXT = """


// noinspection TypeScriptUnresolvedReference
const SteamClient = {
    Apps: Apps,
    Auth: Auth,
    Broadcast: Broadcast,
    Browser: Browser,
    BrowserView: BrowserView,
    ClientNotifications: ClientNotifications,
    Cloud: Cloud,
    CommunityItems: CommunityItems,
    Console: Console,
    Customization: Customization,
    Downloads: Downloads,
    FamilySharing: FamilySharing,
    Friends: Friends,
    FriendSettings: FriendSettings,
    GameNotes: GameNotes,
    GameSessions: GameSessions,
    Input: Input,
    InstallFolder: InstallFolder,
    Installs: Installs,
    MachineStorage: Storage,
    Messaging: Messaging,
    Music: Music,
    Notifications: Notifications,
    OpenVR: OpenVR,
    Overlay: Overlay,
    Parental: Parental,
    RemotePlay: RemotePlay,
    RoamingStorage: Storage,
    Screenshots: Screenshots,
    ServerBrowser: ServerBrowser,
    Settings: Settings,
    SharedConnection: SharedConnection,
    Stats: Stats,
    SteamChina: SteamChina,
    Storage: Storage,
    Streaming: Streaming,
    System: System,
    UI: UI,
    URL: URL,
    Updates: Updates,
    User: User,
    WebChat: WebChat,
    WebUITransport: WebUITransport,
    Window: Window,
}

            """;
}
