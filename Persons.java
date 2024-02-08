import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Persons {
    private String name;
    private String email;
    private boolean isAvailable;
    private Date availabilityTime;
    private List<Date> additionalSchedules;
    private List<Date> schedule;

    public Persons(String name, String email, List<Date> schedule) {
        this.name = name;
        this.email = email;
        this.additionalSchedules = new ArrayList<>();
        this.schedule = schedule; // Initialize schedule with the provided schedule argument
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Date getAvailabilityTime() {
        return availabilityTime;
    }

    public void setAvailabilityTime(Date availabilityTime) {
        this.availabilityTime = availabilityTime;
    }

    public void addAdditionalSchedule(Date additionalSchedule) {
        additionalSchedules.add(additionalSchedule);
    }

    public List<Date> getAdditionalSchedules() {
        return additionalSchedules;
    }
    public List<Date> getSchedule() {
        return schedule;
    }
    //////Return the name of a person//////
    @Override
    public String toString() {
        return name;
    }
    /////Check if two Persons objects are equal.
    ///////It first checks if the objects are the same instance (using reference equality).
    //////If not, it checks if the objects are instances of the same class and have the same name and email.
    /////If both conditions are true, it considers the objects equal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persons person = (Persons) o;
        return Objects.equals(name, person.name) && Objects.equals(email, person.email);
    }
    ///////Generate Hash code based on name and email fields of the "Person" object/////
    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}
