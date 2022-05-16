import java.util.ArrayList;
import java.util.Iterator;

public class Party {

    private ArrayList<Participant> party;

    public void AddParticipant(Participant p) {
        party.add(p);
    }

    public Participant GetParticipant(String name) {
        Iterator<Participant> ite = party.iterator();
        while (ite.hasNext()) {
            Participant p = ite.next(); ite.remove();
            if (p.GetName().equals(name)) return p;
        }
        return null;
    }

    public void RemoveParticipant(String name) {
        Participant p = GetParticipant(name);
        if (p == null) return;
        party.remove(p);
    }

    public ArrayList<Participant> GetAllParticipants() {
        return party;
    }
}
