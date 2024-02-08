import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Schedulingapp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Scheduler scheduler = new Scheduler();

        /////Create persons with unique emails
        createPersonsFromInput(scheduler, scanner);

        /////Retrieve persons
        List<Persons> persons = scheduler.retrievePersons();
        System.out.println("Persons: " + persons);

        /////Ask each person for their availability
        askForAvailability(persons, scheduler, scanner);

        ///// Suggest available times
        List<Date> suggestedTimes = scheduler.suggestAvailableTime(persons, 5);
        scheduler.displaySuggestedTimes(suggestedTimes, 5);

        /////Show upcoming meetings after suggesting times for each person
        for (Persons person : persons) {

            /////// Show additional schedules for the person//////////////
            List<Date> additionalSchedules = scheduler.getAdditionalSchedulesForPerson(person);
            if (!additionalSchedules.isEmpty()) {
                System.out.println(person.getName() + " has additional schedules:");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                for (Date schedule : additionalSchedules) {
                    System.out.println(dateFormat.format(schedule));
                }
            } else {
                System.out.println(person.getName() + " has no additional schedules.");
            }
        }

        /////////Create a meeting//////////
        createMeeting(scheduler, persons, scanner);
    }

    private static void createPersonsFromInput(Scheduler scheduler, Scanner scanner) {
        System.out.println("Enter the number of persons you want to create:");
        int numberOfPersons = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        for (int i = 0; i < numberOfPersons; i++) {
            System.out.println("Enter the name for person " + (i + 1) + ":");
            String name = scanner.nextLine();

            /////// Generate email based on the name/////////
            String generatedEmail = generateEmailFromName(name);

            /////////Attempt to create a person with the generated email//////////
            createUniquePerson(scheduler, name, generatedEmail);
        }
    }
    ////////Ask for availabilities//////////////
    private static void askForAvailability(List<Persons> participants, Scheduler scheduler, Scanner scanner) {
        System.out.println("Enter availability for each participant:");
        for (Persons person : participants) {
            System.out.println("Availability for " + person.getName() + ":");
            // Ask for availability time
            System.out.println("Enter availability time for " + person.getName() + " (yyyy-MM-dd hh:mm:ss a):");
            String availabilityTimeInput = scanner.nextLine();
            try {
                //////// Parse availability time with PHT time zone/////////
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
                inputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                Date availabilityTime = inputDateFormat.parse(availabilityTimeInput);
                ////// Check if the participant has any schedule conflicts//////////////
                if (scheduler.hasScheduleConflict(person, availabilityTime)) {
                    System.out.println("Sorry, " + person.getName() + " has a schedule conflict at that time.");
                    continue; // Skip further processing for this participant
                }

                ///////// Ask if the participants has other schedules///////////
                System.out.println("Does " + person.getName() + " have other schedules? (yes/no):");
                String response = scanner.nextLine().toLowerCase();
                if (response.equals("yes")) {
                    // Ask for additional schedules
                    System.out.println("Enter the time and date of the additional schedule for " + person.getName() + " (yyyy-MM-dd hh:mm:ss a):");
                    String additionalScheduleInput = scanner.nextLine();
                    Date additionalSchedule = inputDateFormat.parse(additionalScheduleInput);
                    // Add the additional schedule to the person's schedule
                    scheduler.addAdditionalSchedule(person, additionalSchedule);
                }
                ///////// Set availability and time in the scheduler////////////
                scheduler.updateAvailability(person, true, availabilityTime);
            } catch (ParseException e) {
                System.out.println("Error parsing availability time: " + e.getMessage());
                System.exit(0); // Exit session
            }
        }
    }
        /////Display Suggested Times/////////
    private static void displaySuggestedTimes(List<Date> suggestedTimes, int count) {
        System.out.println("Suggested Times (PHT):");
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        outputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

        // This Limit the count of suggested times to display
        int displayCount = Math.min(count, suggestedTimes.size());

        for (int i = 0; i < displayCount; i++) {
            System.out.println(outputDateFormat.format(suggestedTimes.get(i)));
        }
    }
    //////Generate email from the name////////
    private static String generateEmailFromName(String name) {
        return name.toLowerCase() + "@reddemo.com";
    }
    ////////////Create Unique Person(input)//////////////
    private static void createUniquePerson(Scheduler scheduler, String name, String email) {
        try {
            scheduler.createPerson(name, email);
            System.out.println("Person created: " + name + " (" + email + ")");
        } catch (IllegalArgumentException e) {
            System.out.println("Error creating person: " + e.getMessage());
        }
    }
    //////////// Create Meeting//////////////////
    private static void createMeeting(Scheduler scheduler, List<Persons> persons, Scanner scanner) {
        List<Persons> meetingParticipants = persons; // Use all available persons for the meeting

        System.out.println("Enter the start time for the meeting (yyyy-MM-dd hh:mm:ss a):");
        String startTimeInput = scanner.nextLine();

        if (startTimeInput == null || startTimeInput.trim().isEmpty()) {
            System.out.println("Invalid start time. Please enter a value.");
            return;
        }

        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));

            Date startTime = inputDateFormat.parse(startTimeInput);

            ///////////Get the end time of the meeting (assuming the meeting duration is 1 hour)////////////
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startTime);
            calendar.add(Calendar.HOUR, 1);
            Date endTime = calendar.getTime();

            //////////Create the meeting/////////////////
            System.out.println("Meeting Created!");
            for (Persons participant : meetingParticipants) {
                System.out.print(participant.getName() + ", ");
            }
            System.out.println();
            System.out.println("Please wait for the meeting to start at " + inputDateFormat.format(startTime));

            //////////////Schedule a task to start the meeting at the specified time/////////////
            ScheduledExecutorService schedulerExecutor = Executors.newScheduledThreadPool(1);
            schedulerExecutor.schedule(() -> {
                scheduler.createMeeting(meetingParticipants, startTime);
                System.out.println("Meeting started!");

                ////////////Schedule a task to end the meeting after one hour/////////////
                schedulerExecutor.schedule(() -> {
                    ////////Perform actions to end the meeting//////////
                    System.out.println("Meeting ended!");
                    schedulerExecutor.shutdown();
                }, 1, TimeUnit.HOURS);
            }, startTime.getTime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            System.out.println("Error parsing the start time. Please enter it in the correct format.");
        }
    }
}
