package vector.HospitalManagementApplication.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.HospitalManagementApplication.models.Appointment;
import vector.HospitalManagementApplication.models.AppointmentStatus;
import vector.HospitalManagementApplication.models.Doctor;
import vector.HospitalManagementApplication.models.Patient;
import vector.HospitalManagementApplication.repositories.AppointmentRepository;
import vector.HospitalManagementApplication.repositories.DoctorRepository;
import vector.HospitalManagementApplication.repositories.PatientRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus(AppointmentStatus.WAITING);
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElseThrow(
                () -> new RuntimeException("Doctor not found with id: " + appointment.getDoctor().getId())
        );
        Patient patient = patientRepository.findById(appointment.getPatient().getId()).orElseThrow(
                () -> new RuntimeException("Patient not found with id: " + appointment.getPatient().getId())
        );

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        doctor.getAppointments().add(appointment);
        patient.getAppointments().add(appointment);


        doctorRepository.save(doctor);
        patientRepository.save(patient);
        return appointmentRepository.save(appointment);


    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> getAppointmentById(Long id) {
        return appointmentRepository.findById(id);
    }

    @Transactional
    public void deleteAppointmentById(Long id) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        if (appointmentOptional.isPresent()) {
            Appointment appointment = appointmentOptional.get();
            if (appointment.getDoctor() != null) {
                appointment.getDoctor().getAppointments().remove(appointment);
            }
            if (appointment.getPatient() != null) {
                appointment.getPatient().getAppointments().remove(appointment);
            }
        }
        appointmentRepository.deleteById(id);
    }

    @Transactional
    public Appointment updateAppointmentById(Long id, Appointment appointment) {
        Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
        //find if the newDoctors are present
        Doctor newDoctor = doctorRepository.findById(appointment.getDoctor().getId()).orElseThrow(
                () -> new RuntimeException("Doctor not found with id: " + appointment.getDoctor().getId())
        );
        Patient newPatient = patientRepository.findById(appointment.getPatient().getId()).orElseThrow(
                () -> new RuntimeException("Patient not found with id: " + appointment.getPatient().getId())
        );



        //remove the current appointment from the appointments in the doctor's and patient list

        if (appointmentOptional.isPresent()) {
            Appointment appointmentToUpdate = appointmentOptional.get();
            if(appointmentToUpdate.getDoctor().getId().equals(appointment.getDoctor().getId())) {
                appointmentToUpdate.getDoctor().getAppointments().remove(appointmentToUpdate);
                doctorRepository.save(appointmentToUpdate.getDoctor());

            }
            if(appointmentToUpdate.getPatient().getId().equals(appointment.getPatient().getId())) {
                appointmentToUpdate.getPatient().getAppointments().remove(appointmentToUpdate);
                patientRepository.save(appointmentToUpdate.getPatient());
            }
            appointmentToUpdate.setAppointmentDate(appointment.getAppointmentDate());
            appointmentToUpdate.setDoctor(appointment.getDoctor());
            appointmentToUpdate.setPatient(appointment.getPatient());
            appointmentToUpdate.setStatus(appointment.getStatus());
            appointmentToUpdate.setReason(appointment.getReason());
            appointmentToUpdate.setTitle(appointment.getTitle());

            //add the appointment to the doctor's and patient's list of appointments
            newDoctor.getAppointments().add(appointmentToUpdate);
            newPatient.getAppointments().add(appointmentToUpdate);



            //save all changes
            doctorRepository.save(newDoctor);
            patientRepository.save(newPatient);
            return appointmentRepository.save(appointmentToUpdate);
        }
        throw new RuntimeException("Appointment not found");
    }

}
