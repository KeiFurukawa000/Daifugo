import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Game {
    private Players playerList;
    private CardBlock cardBlock;

    private int endedGameCount;

    public void Start(Party readyListParty) {

        cardBlock = new CardBlock();

        Iterator<Participant> ite = readyListParty.GetAllParticipants().iterator();
        while (ite.hasNext()) {
            Participant p = ite.next(); ite.remove();
            Hand hand = cardBlock.Deal();
            Player player = new Player(p.GetName(), hand);
            playerList.AddPlayer(player);;
        }
        ChangeSeat();
    }

    private void ChangeSeat() {
        // 最初のゲーム
        if (endedGameCount == 0) {
            Collections.shuffle(playerList.GetPlayers());
            Iterator<Player> ite = playerList.GetPlayers().iterator();
            while (ite.hasNext()) {
                Player player = ite.next(); ite.remove();
                if (player.IsMaster()) {
                    playerList.RemovePlayer(player);
                    playerList.AddPlayer(0, player);
                }
            }
        }
    }

    public void Turn() {
        Iterator<Player> ite = playerList.GetPlayers().iterator();
        while (ite.hasNext()) {
            Player player = ite.next(); ite.remove();

            // カードがない場合、あがり
            if (player.IsHandEmpty()) {

                continue;
            }
        }
    }

    public ArrayList<Card> GetCards(String str) {
        String[] cmd = str.split(" ");
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < cmd.length; i++) {
            String[] set = cmd[i].split("/");
            String icon = set[0];
            int number = Integer.parseInt(set[1]);
            cards.add(new Card(icon, number));
        }
        return cards;
    }

    public void Put(String name, String cardsStr) {
        Player player = playerList.GetPlayer(name);
        ArrayList<Card> cards = GetCards(cardsStr);
        player.Put(cards.toArray(new Card[cards.size()]));
    }

    public void Pass(Player player) {

    }

    public void Revolution(Player player, int...cards) {

    }
}
