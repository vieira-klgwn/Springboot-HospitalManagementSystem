package vector.HospitalManagementApplication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.HospitalManagementApplication.models.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
