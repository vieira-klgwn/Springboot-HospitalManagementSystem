```java
        package com.example.hospitalmanagementsystem.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String specialty;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    // Helper method to add appointment
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setDoctor(this);
    }
}
```

        ```java
package com.example.hospitalmanagementsystem.entity;

import jakarta.persistence.*;
        import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String contactNumber;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }

    // Helper method to add appointment
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setPatient(this);
    }
}
```

        ```java
package com.example.hospitalmanagementsystem.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    private LocalDateTime appointmentDateTime;
    private String reason;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
```

        ### Explanation of Entity Relationships

- **Doctor to Appointment**: A `OneToMany` relationship is added to the `Doctor` entity with `mappedBy = "doctor"`, indicating that the `Appointment` entity owns the relationship. The `cascade = CascadeType.ALL` ensures that operations (like persist or delete) on a Doctor cascade to its appointments, and `orphanRemoval = true` removes appointments if they are removed from the list.
        - **Patient to Appointment**: Similarly, a `OneToMany` relationship is added to the `Patient` entity with `mappedBy = "patient"`.
        - **Appointment**: Retains its `ManyToOne` relationships with `Doctor` and `Patient`. The `JoinColumn` annotations specify the foreign key columns (`doctor_id` and `patient_id`) in the `Appointment` table.
- **Helper Methods**: The `addAppointment` methods in `Doctor` and `Patient` ensure bidirectional consistency by updating both sides of the relationship when an appointment is added.

### Updated AppointmentService

The `AppointmentService` will be updated to handle the bidirectional relationship when creating or updating appointments, ensuring that the appointment is added to both the `Doctor` and `Patient` appointment lists.

<xaiArtifact artifact_id="4d606994-5aea-4a7a-8d79-e591bb051c25" artifact_version_id="696a0fa0-2f8f-4a85-a73a-cb52c5d3036c" title="HospitalManagementSystem" contentType="text/java">
        ```java
package com.example.hospitalmanagementsystem.service;

import com.example.hospitalmanagementsystem.entity.Appointment;
import com.example.hospitalmanagementsystem.entity.Doctor;
import com.example.hospitalmanagementsystem.entity.Patient;
import com.example.hospitalmanagementsystem.repository.AppointmentRepository;
import com.example.hospitalmanagementsystem.repository.DoctorRepository;
import com.example.hospitalmanagementsystem.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional
    public Appointment saveAppointment(Appointment appointment) {
        // Fetch the Doctor and Patient entities
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + appointment.getDoctor().getId()));
        Patient patient = patientRepository.findById(appointment.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + appointment.getPatient().getId()));

        // Set the relationships
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        // Add appointment to Doctor and Patient lists
        doctor.addAppointment(appointment);
        patient.addAppointment(appointment);

        // Save the entities
        appointmentRepository.save(appointment);
        doctorRepository.save(doctor);
        patientRepository.save(patient);

        return appointment;
    }

    @Transactional
    public Appointment updateAppointment(Long id, Appointment appointmentDetails) {
        Optional<Appointment> existingAppointmentOpt = appointmentRepository.findById(id);
        if (!existingAppointmentOpt.isPresent()) {
            throw new IllegalArgumentException("Appointment not found with ID: " + id);
        }

        Appointment existingAppointment = existingAppointmentOpt.get();

        // Fetch the new Doctor and Patient if they are different
        Doctor newDoctor = doctorRepository.findById(appointmentDetails.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + appointmentDetails.getDoctor().getId()));
        Patient newPatient = patientRepository.findById(appointmentDetails.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + appointmentDetails.getPatient().getId()));

        // Remove appointment from old Doctor and Patient lists
        if (existingAppointment.getDoctor() != null && !existingAppointment.getDoctor().getId().equals(newDoctor.getId())) {
            existingAppointment.getDoctor().getAppointments().remove(existingAppointment);
            doctorRepository.save(existingAppointment.getDoctor());
        }
        if (existingAppointment.getPatient() != null && !existingAppointment.getPatient().getId().equals(newPatient.getId())) {
            existingAppointment.getPatient().getAppointments().remove(existingAppointment);
            patientRepository.save(existingAppointment.getPatient());
        }

        // Update appointment details
        existingAppointment.setDoctor(newDoctor);
        existingAppointment.setPatient(newPatient);
        existingAppointment.setAppointmentDateTime(appointmentDetails.getAppointmentDateTime());
        existingAppointment.setReason(appointmentDetails.getReason());

        // Add appointment to new Doctor and Patient lists
        newDoctor.addAppointment(existingAppointment);
        newPatient.addAppointment(existingAppointment);

        // Save all changes
        appointmentRepository.save(existingAppointment);
        doctorRepository.save(newDoctor);
        patientRepository.save(newPatient);

        return existingAppointment;
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(id);
        if (appointmentOpt.isPresent()) {
            Appointment appointment = appointmentOpt.get();
            // Remove appointment from Doctor and Patient lists
            if (appointment.getDoctor() != null) {
                appointment.getDoctor().getAppointments().remove(appointment);
                doctorRepository.save(appointment.getDoctor());
            }
            if (appointment.getPatient() != null) {
                appointment.getPatient().getAppointments().remove(appointment);
                patientRepository.save(appointment.getPatient());
            }
            appointmentRepository.deleteById(id);
        }
    }
}
```

        ### Explanation of AppointmentService

- **Dependencies**: The service now includes `DoctorRepository` and `PatientRepository` to fetch and update `Doctor` and `Patient` entities.
- **Transactional Annotation**: The `@Transactional` annotation ensures that database operations are atomic, maintaining data consistency.
- **saveAppointment**:
        - Fetches the `Doctor` and `Patient` by their IDs.
  - Sets the relationships in the `Appointment` entity.
  - Uses the `addAppointment` helper methods to maintain the bidirectional relationship by adding the appointment to both `Doctor` and `Patient` lists.
  - Saves the `Appointment`, `Doctor`, and `Patient` to persist the relationships.
        - **updateAppointment**:
        - Fetches the existing appointment and validates the new `Doctor` and `Patient`.
        - Removes the appointment from the old `Doctor` and `Patient` lists if they have changed.
  - Updates the appointment details and adds it to the new `Doctor` and `Patient` lists.
  - Saves all changes.
- **deleteAppointment**:
        - Removes the appointment from the `Doctor` and `Patient` lists before deleting it from the database to maintain consistency.

        ### Updated Controller

The `AppointmentController` remains largely the same, but I'll include it for completeness to show how it integrates with the updated service:

<xaiArtifact artifact_id="4d606994-5aea-4a7a-8d79-e591bb051c25" artifact_version_id="6f0cf439-64ff-4b96-a385-d7b507b03288" title="HospitalManagementSystem" contentType="text/java">
        ```java
package com.example.hospitalmanagementsystem.controller;

import com.example.hospitalmanagementsystem.entity.Appointment;
import com.example.hospitalmanagementsystem.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Optional<Appointment> appointment = appointmentService.getAppointmentById(id);
        return appointment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Appointment createAppointment(@RequestBody Appointment appointment) {
        return appointmentService.saveAppointment(appointment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable Long id, @RequestBody Appointment appointmentDetails) {
        try {
            Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentDetails);
            return ResponseEntity.ok(updatedAppointment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok().build();
    }
}
```

        ### How to Test

1. **Setup**: Ensure the project has the same dependencies and configuration as in the original example (Spring Boot Web, Data JPA, H2).
        2. **Run the Application**: Start the Spring Boot application.
3. **Test with API Calls**:
        - Create a doctor: `POST /api/doctors` with `{ "name": "Dr. Smith", "specialty": "Cardiology" }`
        - Create a patient: `POST /api/patients` with `{ "name": "John Doe", "dateOfBirth": "1990-01-01", "contactNumber": "1234567890" }`
        - Create an appointment: `POST /api/appointments` with `{ "doctor": { "id": 1 }, "patient": { "id": 1 }, "appointmentDateTime": "2025-06-05T10:00:00", "reason": "Checkup" }`
        - Verify the appointment is added to both Doctor and Patient: `GET /api/doctors/1` and `GET /api/patients/1` should show the appointment in their respective `appointments` lists.

### Notes

- **Bidirectional Consistency**: The `addAppointment` helper methods ensure that both sides of the relationship are updated, preventing inconsistencies.
        - **Transactional Management**: The `@Transactional` annotation ensures that all database operations (saving appointment, updating doctor and patient lists) are completed together or rolled back if an error occurs.
- **Error Handling**: The service throws `IllegalArgumentException` if the doctor or patient is not found. You might want to add custom exceptions and better error handling in a production system.
- **Enhancements**: For a production system, consider adding:
        - Validation for appointment dates (e.g., no past dates).
        - Conflict checking (e.g., no overlapping appointments for a doctor).
        - Authentication to restrict access to certain operations.
  - Input validation using `@Valid` and Bean Validation annotations.

This implementation provides a robust way to model the relationships and manage appointments in the Hospital Management System. Let me know if you need further clarification or additional features!