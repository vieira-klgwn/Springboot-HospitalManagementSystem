package vector.HospitalManagementApplication.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vector.HospitalManagementApplication.models.Appointment;
import vector.HospitalManagementApplication.services.AppointmentService;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> addAppointment(@RequestBody Appointment appointment) {
        return new ResponseEntity<>(appointment, HttpStatus.CREATED);
    }
    @GetMapping
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable("id") Long id) {
        try {
            appointmentService.getAppointmentById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> updateAppointment(@PathVariable("id") Long id, @RequestBody Appointment appointment) {
        try {
            appointmentService.updateAppointmentById(id, appointment);
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Appointment> deleteAppointment(@PathVariable("id") Long id) {
        try {
            appointmentService.deleteAppointmentById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
