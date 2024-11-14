import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Doctor {
    private Connection connection;

    public Doctor(Connection connection) {
        this.connection = connection;
    }

    public String viewDoctors() {
        StringBuilder doctorList = new StringBuilder();
        String query = "SELECT * FROM doctors";
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            doctorList.append("Doctors:\n");
            doctorList.append("ID\tName\tSpecialization\n");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String specialization = resultSet.getString("specialization");
                doctorList.append(id).append("\t").append(name).append("\t").append(specialization).append("\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            doctorList.append("Error retrieving doctor data.");
        }
        return doctorList.toString();
    }
}
