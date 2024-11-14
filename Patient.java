import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Patient {
    private Connection connection;

    public Patient(Connection connection) {
        this.connection = connection;
    }

    public void addPatient(String name, int age, String gender) {
        try {
            String query = "INSERT INTO patients(name, age, gender) VALUES(?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, age);
            preparedStatement.setString(3, gender);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String viewPatients() {
        StringBuilder patientList = new StringBuilder();
        String query = "SELECT * FROM patients";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            patientList.append("Patients:\n");
            patientList.append("ID\tName\tAge\tGender\n");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int age = resultSet.getInt("age");
                String gender = resultSet.getString("gender");
                patientList.append(id).append("\t").append(name).append("\t").append(age).append("\t").append(gender).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            patientList.append("Error retrieving patient data.");
        }
        return patientList.toString();
    }
}
