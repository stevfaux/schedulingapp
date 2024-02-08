import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private List<Persons> persons;
    private List<Meetings> meetings;

    public Scheduler() {
        persons = new ArrayList<>();
        meetings = new ArrayList<>();
    }
    ///////////// Create Person///////////
    public void createPerson(String name, String email) {
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }
        Persons person = new Persons(name, email, new ArrayList<>());
        persons.add(person);
    }
    /////// Check if the email already exists///////////
    private boolean emailExists(String email) {
        for (Persons person : persons) {
            if (person.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }
    //////////////// Find Person////////////////////
    public List<Persons> findPersonsByName(List<Persons> persons, String[] names) {
        List<Persons> foundPersons = new ArrayList<>();
        for (String name : names) {
            for (Persons person : persons) {
                if (person.getName().equalsIgnoreCase(name.trim())) {
                    foundPersons.add(person);
                    break;
                }
            }
        }
        return foundPersons;
    }
    ////////////Create meeting////////////
    public void createMeeting(List<Persons> participants, Date startTime) {
        validateMeetingTime(startTime);

        Meetings meeting = new Meetings(participants, startTime);
        meetings.add(meeting);
    }
    //////////Validate meeeting////////////
    private void validateMeetingTime(Date startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);

        if (calendar.get(Calendar.MINUTE) != 0 || calendar.get(Calendar.SECOND) != 0) {
            throw new IllegalArgumentException("Meeting must start at the hour mark.");
        }
    }
    ////////Get schedule of a person////////////////
    public List<Meetings> getScheduleForPerson(String personEmail) {
        List<Meetings> schedule = new ArrayList<>();
        Date currentTime = new Date(); // Get the current time
        for (Meetings meeting : meetings) {
            for (Persons person : meeting.getParticipants()) {
                if (person.getEmail().equals(personEmail)) {
                    //////////Check if the meeting start time is after or equal to the current time//////////
                    if (meeting.getStartTime().after(currentTime) || meeting.getStartTime().equals(currentTime)) {
                        ///////////Add the meeting to the schedule////////////
                        schedule.add(meeting);
                    }
                    break;
                }
            }
        }
        return schedule;
    }
    /////////////Fetch the additional schedule of a person///////////
    public List<Date> getUpcomingMeetings(Persons person) {
        List<Date> upcomingMeetings = new ArrayList<>();
        ////Get the current date and time in PHT////
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Manila"));
        Date currentDate = calendar.getTime();

        ///////Iterate through the person's schedule//////
        List<Date> personSchedule = person.getSchedule();
        for (Date meetingDate : personSchedule) {
            ////Check if the meeting date is after the current date/////
            if (meetingDate.after(currentDate)) {
                upcomingMeetings.add(meetingDate);
            }
        }

        return upcomingMeetings;
    }

    //////Show upcoming schedules/////////
    public List<Date> showScheduleForPerson(Persons person) {
        List<Date> schedule = new ArrayList<>();
        Date currentTime = new Date(); // Get the current time
        for (Meetings meeting : meetings) {
            for (Persons participant : meeting.getParticipants()) {
                if (participant.equals(person)) {
                    // Check if the meeting start time is after or equal to the current time
                    if (meeting.getStartTime().after(currentTime) || meeting.getStartTime().equals(currentTime)) {
                        // Add the meeting to the schedule
                        schedule.add(meeting.getStartTime());
                    }
                    break;
                }
            }
        }
        return schedule;
    }
    //////////Suggest available time except the time where a person have additional meeting/////////
    public List<Date> suggestAvailableTime(List<Persons> participants, int numberOfSuggestions) {
        /////////Define the duration of the meeting (in milliseconds), assuming 1 hour///////////
        long meetingDuration = TimeUnit.HOURS.toMillis(1);
        /////////Initialize a list to store suggested times/////////////
        List<Date> suggestedTimes = new ArrayList<>();

        ////////////Get the current time/////////////////
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(new Date());

        ///////////Iterate over each hour of the next 24 hours////////////
        for (int i = 0; i < 24; i++) {
            //////Calculate the start time of the current hour/////
            Calendar suggestedTime = Calendar.getInstance();
            suggestedTime.setTime(currentTime.getTime());
            suggestedTime.set(Calendar.MINUTE, 0);
            suggestedTime.set(Calendar.SECOND, 0);
            suggestedTime.add(Calendar.HOUR_OF_DAY, i);

            ///////Check availability for each participant/////////
            boolean available = true;
            for (Persons person : participants) {
                ////Check if there's any schedule conflict for this hour///
                for (Date schedule : person.getAdditionalSchedules()) {
                    Calendar scheduleTime = Calendar.getInstance();
                    scheduleTime.setTime(schedule);
                    //////Check if the schedule falls within the current hour/////
                    if (scheduleTime.get(Calendar.HOUR_OF_DAY) == suggestedTime.get(Calendar.HOUR_OF_DAY)) {
                        available = false;
                        break;
                    }
                }
                if (!available) {
                    break;
                }
            }

            ///////If no conflicts found, add the suggested time to the list///////
            if (available) {
                suggestedTimes.add(suggestedTime.getTime());
                ////////If enough suggestions have been made, return the list////
                if (suggestedTimes.size() >= numberOfSuggestions) {
                    return suggestedTimes;
                }
            }
        }
        return suggestedTimes;
    }
    ///////Update availability////////
    public void updateAvailability(Persons person, boolean isAvailable, Date availabilityTime) {
        for (Persons p : persons) {
            if (p.equals(person)) {
                p.setAvailable(isAvailable);
                p.setAvailabilityTime(availabilityTime);
                break;
            }
        }
    }
    ////////Check for schedule conflicts//////
    public boolean hasScheduleConflict(Persons person, Date time) {
        for (Meetings meeting : meetings) {
            for (Persons participant : meeting.getParticipants()) {
                if (participant.equals(person)) {
                    // Check if the meeting overlaps with the specified time
                    if (meeting.getStartTime().equals(time)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    ////////Add additional Schedule(upcoming meetings)//////////
    public void addAdditionalSchedule(Persons person, Date additionalSchedule) {
        for (Persons p : persons) {
            if (p.equals(person)) {
                p.addAdditionalSchedule(additionalSchedule);
                break;
            }
        }
    }
    ///////////Display the suggested time in PHT///////////
    public void displaySuggestedTimes(List<Date> suggestedTimes, int count) {
        System.out.println("Suggested Times (PHT):");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        outputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        ////////This Limit the count of suggested times to display/////////
        int displayCount = Math.min(count, suggestedTimes.size());

        for (int i = 0; i < displayCount; i++) {
            System.out.println(outputDateFormat.format(suggestedTimes.get(i)));
        }
    }
    ////////Retrieve Person to show Persons created/////////
    public List<Persons> retrievePersons() {
        return persons;
    }
    //////////////Show upcoming meetings of a person////////
    public void showUpcomingMeetingsForPerson(Persons person) {
        List<Date> upcomingMeetings = getUpcomingMeetings(person);
        if (upcomingMeetings.isEmpty()) {
            System.out.println(person.getName() + " has no upcoming meetings.");
        } else {
            System.out.println("Upcoming meetings for " + person.getName() + ":");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
            for (Date meeting : upcomingMeetings) {
                System.out.println(dateFormat.format(meeting));
            }
        }
    }
    ///////Get additional schedules of a person/////////////
    public List<Date> getAdditionalSchedulesForPerson(Persons person) {
    List<Date> additionalSchedules = person.getAdditionalSchedules();
    if (additionalSchedules == null) {
        return Collections.emptyList(); // Return an empty list if additionalSchedules is null
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
    dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
    List<Date> additionalSchedulesInPHT = new ArrayList<>();
    for (Date schedule : additionalSchedules) {
        additionalSchedulesInPHT.add(schedule);
    }
    return additionalSchedulesInPHT;
    }
}
