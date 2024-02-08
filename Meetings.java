import java.util.Date;
import java.util.List;

public class Meetings {
    private List<Persons> participants;
    private Date startTime;

    public Meetings(List<Persons> participants, Date startTime) {
        this.participants = participants;
        this.startTime = startTime;
    }

    public List<Persons> getParticipants() {
        return participants;
    }

    public Date getStartTime() {
        return startTime;
    }
}
