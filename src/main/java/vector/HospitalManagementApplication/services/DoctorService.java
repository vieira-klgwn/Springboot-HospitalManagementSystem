package vector.HospitalManagementApplication.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.HospitalManagementApplication.models.Doctor;
import vector.HospitalManagementApplication.repositories.DoctorRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;

    public Doctor save(Doctor doctor) {
        return doctorRepository.save(doctor);
    }
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> findById(Long id) {
        return doctorRepository.findById(id);
    }
    public void deleteById(Long id) {
        doctorRepository.deleteById(id);
    }
    public Doctor updateDoctor(Long id,Doctor doctor) {
        Optional<Doctor> doctorOptional = doctorRepository.findById(id);
        if (doctorOptional.isPresent()) {
            Doctor doctorToUpdate = doctorOptional.get();
            doctorToUpdate.setSpeciality(doctor.getSpeciality());
            return doctorRepository.save(doctor);
        }
        throw new RuntimeException("Doctor not found");
    }

}
