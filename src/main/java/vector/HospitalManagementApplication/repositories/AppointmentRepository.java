package vector.HospitalManagementApplication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.HospitalManagementApplication.models.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
}
