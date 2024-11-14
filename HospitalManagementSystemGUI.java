import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class HospitalManagementSystemGUI {
    private static final String url = "jdbc:mysql://localhost:3306/hospital"; 
    private static final String username = "root"; 
    private static final String password = "shreedhar@123"; 

    private Connection connection;
    private Patient patient;
    private Doctor doctor;

    public HospitalManagementSystemGUI() {
        initializeDatabaseConnection();
        initializeGUI();
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
            patient = new Patient(connection);
            doctor = new Doctor(connection);
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection failed! Please check your configuration.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeGUI() {
        JFrame frame = new JFrame("Hospital Management System");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        frame.getContentPane().setBackground(Color.DARK_GRAY);

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Hospital Management System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(Color.CYAN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        frame.add(titleLabel, gbc);

        JButton addPatientButton = createStyledButton("Add Patient", buttonFont);
        addPatientButton.addActionListener(e -> addPatientGUI());
        gbc.gridy++;
        frame.add(addPatientButton, gbc);

        JButton viewPatientsButton = createStyledButton("View Patients", buttonFont);
        viewPatientsButton.addActionListener(e -> viewPatientsGUI());
        gbc.gridy++;
        frame.add(viewPatientsButton, gbc);

        JButton viewDoctorsButton = createStyledButton("View Doctors", buttonFont);
        viewDoctorsButton.addActionListener(e -> viewDoctorsGUI());
        gbc.gridy++;
        frame.add(viewDoctorsButton, gbc);

        JButton bookAppointmentButton = createStyledButton("Book Appointment", buttonFont);
        bookAppointmentButton.addActionListener(e -> bookAppointmentGUI());
        gbc.gridy++;
        frame.add(bookAppointmentButton, gbc);

        JButton exitButton = createStyledButton("Exit", buttonFont);
        exitButton.addActionListener(e -> System.exit(0));
        gbc.gridy++;
        frame.add(exitButton, gbc);

        frame.setVisible(true);
    }

    private JButton createStyledButton(String text, Font font) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setBackground(Color.GRAY);
        return button;
    }

    private void addPatientGUI() {
        JTextField nameField = new JTextField(10);
        JTextField ageField = new JTextField(10);
        JTextField genderField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Enter Patient Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Enter Patient Age:"));
        panel.add(ageField);
        panel.add(new JLabel("Enter Patient Gender:"));
        panel.add(genderField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Patient", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                int age = Integer.parseInt(ageField.getText());
                String gender = genderField.getText();
                patient.addPatient(name, age, gender);
                JOptionPane.showMessageDialog(null, "Patient added successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid age entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewPatientsGUI() {
        String[] columnNames = {"ID", "Name", "Age", "Gender"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM patients");
            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getInt("id"), resultSet.getString("name"), resultSet.getInt("age"), resultSet.getString("gender")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Patient List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewDoctorsGUI() {
        String[] columnNames = {"ID", "Name", "Specialization"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        try {
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM doctors");
            while (resultSet.next()) {
                model.addRow(new Object[]{resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("specialization")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JTable table = new JTable(model);
        JOptionPane.showMessageDialog(null, new JScrollPane(table), "Doctor List", JOptionPane.INFORMATION_MESSAGE);
    }

    private void bookAppointmentGUI() {
        JTextField patientIdField = new JTextField(10);
        JTextField doctorIdField = new JTextField(10);
        JTextField dateField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Enter Patient ID:"));
        panel.add(patientIdField);
        panel.add(new JLabel("Enter Doctor ID:"));
        panel.add(doctorIdField);
        panel.add(new JLabel("Enter Appointment Date (YYYY-MM-DD):"));
        panel.add(dateField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Book Appointment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int patientId = Integer.parseInt(patientIdField.getText());
                int doctorId = Integer.parseInt(doctorIdField.getText());
                String appointmentDate = dateField.getText();

                if (!doesPatientExist(patientId) || !doesDoctorExist(doctorId)) {
                    JOptionPane.showMessageDialog(null, "Invalid patient or doctor ID. Please verify.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (checkDoctorAvailability(doctorId, appointmentDate)) {
                    bookAppointment(patientId, doctorId, appointmentDate);
                    JOptionPane.showMessageDialog(null, "Appointment booked successfully!");
                } else {
                    JOptionPane.showMessageDialog(null, "Doctor not available on this date. Please choose another date.", "Error", JOptionPane.WARNING_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid ID entered.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(null, "Invalid date format. Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Failed to book appointment. Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean checkDoctorAvailability(int doctorId, String appointmentDate) throws SQLException {
        String query = "SELECT COUNT(*) FROM appointments WHERE doctor_id = ? AND appointment_date = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0; // True if no appointments exist on this date
            }
        }
        return false;
    }

    private boolean doesPatientExist(int patientId) throws SQLException {
        String query = "SELECT COUNT(*) FROM patients WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

    private boolean doesDoctorExist(int doctorId) throws SQLException {
        String query = "SELECT COUNT(*) FROM doctors WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();
            return resultSet.getInt(1) > 0;
        }
    }

    private void bookAppointment(int patientId, int doctorId, String appointmentDate) throws SQLException {
        String query = "INSERT INTO appointments(patient_id, doctor_id, appointment_date) VALUES(?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, patientId);
            preparedStatement.setInt(2, doctorId);
            preparedStatement.setDate(3, Date.valueOf(appointmentDate));
            preparedStatement.executeUpdate();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HospitalManagementSystemGUI::new);
    }
}
