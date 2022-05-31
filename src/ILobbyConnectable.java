

public interface ILobbyConnectable {

    void RequestJoinLobby(String text, String text2, String name);

    void RequestCreateLobby(String text, String name);

    void SendChat(String lobbyName, String name, String text);

    void RequestLeaveLobby(String lobbyName, String name);

    void RequestReady(String lobbyName, String name);

    void RequestUnready(String lobbyName, String name);

    void SendGameOptions(String name, String lobbyName, String text);

    void RequestStartGame(String lobbyName, String name);

    void RequestDeleteAccount(String name);
}
