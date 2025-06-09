package vector.HospitalManagementApplication.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vector.HospitalManagementApplication.models.Doctor;
import vector.HospitalManagementApplication.services.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        try {
            doctorService.save(doctor);
            return new ResponseEntity<>(doctor, HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {

        return new ResponseEntity<>(doctorService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id) {
       try {
           doctorService.findById(id);
           return new ResponseEntity<>(HttpStatus.OK);
       }catch (Exception e){
           return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
       }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        try {
            doctorService.updateDoctor(id, doctor);
            return new ResponseEntity<>(doctor, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Doctor> deleteDoctor(@PathVariable Long id) {
        try{
            doctorService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
