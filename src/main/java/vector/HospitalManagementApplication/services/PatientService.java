package vector.HospitalManagementApplication.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.HospitalManagementApplication.models.Patient;
import vector.HospitalManagementApplication.repositories.PatientRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Optional <Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    public void delete(Long id) {
        patientRepository.deleteById(id);
    }

    public Patient update(Long id, Patient patient) {
        Optional<Patient> patientOptional = patientRepository.findById(patient.getId());
        if (patientOptional.isPresent()) {
            Patient patientToUpdate = patientOptional.get();
            patientToUpdate.setPatient(patient.getPatient());
            patientToUpdate.setPatient(patientToUpdate.getPatient());
            return patientRepository.save(patientToUpdate);
        }
        throw new RuntimeException("Patient not found");
    }


}
