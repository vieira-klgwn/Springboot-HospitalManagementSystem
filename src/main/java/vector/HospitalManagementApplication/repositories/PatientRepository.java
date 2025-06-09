package vector.HospitalManagementApplication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.HospitalManagementApplication.models.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
