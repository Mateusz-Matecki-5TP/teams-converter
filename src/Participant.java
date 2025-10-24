public class Participant {
    String firstName;
    String lastName;
    String time;
    String status;

    Participant(String firstName, String lastName, String time) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.time = time;
        this.status = isCertificate(time);
    }

    public String isCertificate(String time) {
        int minutes = TeamsConverter.convertToMinutes(time);

        if(minutes >= 60) {
            return "Tak";
        }

        return "Nie";
    }

    public String toCSV() {
        return lastName + "," + firstName + "," + time + "," + status;
    }
}
