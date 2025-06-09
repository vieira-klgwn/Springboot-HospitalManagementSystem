package Ntwali.Ntwali_Isimbi;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

// Main Application
@SpringBootApplication
public class EquipmentManagementSystem {
    public static void main(String[] args) {
        SpringApplication.run(EquipmentManagementSystem.class, args);
    }
}

// Equipment Entity
@Entity
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    private int quantity;
    private String status; // AVAILABLE, IN_USE, MAINTENANCE
    private String location;
}

// Request Entity
@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class EquipmentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long equipmentId;
    private String purpose;
    private LocalDateTime requestDate;
    private String status; // PENDING, APPROVED, REJECTED
    private String duration;
}

// Return Entity
@Entity
@Table(name = "returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class EquipmentReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long requestId;
    private LocalDateTime returnDate;
    private String condition; // GOOD, DAMAGED
}

// Allocation Log Entity
@Entity
@Table(name = "allocation_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AllocationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long equipmentId;
    private Long userId;
    private LocalDateTime allocatedAt;
    private String action; // ALLOCATED
}

// Repositories
interface EquipmentRepository extends JpaRepository<Equipment, Long> {}
interface RequestRepository extends JpaRepository<EquipmentRequest, Long> {}
interface ReturnRepository extends JpaRepository<EquipmentReturn, Long> {}
interface AllocationLogRepository extends JpaRepository<AllocationLog, Long> {}

// Equipment Controller
@RestController
@RequestMapping("/api/equipment")
class EquipmentController {
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Equipment> addEquipment(@RequestBody Equipment equipment) {
        return new ResponseEntity<>(equipmentService.addEquipment(equipment), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipment));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }
}

// Request Controller
@RestController
@RequestMapping("/api/requests")
class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<EquipmentRequest> submitRequest(@RequestBody EquipmentRequest request) {
        return new ResponseEntity<>(requestService.submitRequest(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentRequest> approveRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.approveRequest(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentRequest> rejectRequest(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.rejectRequest(id));
    }
}

// Return Controller
@RestController
@RequestMapping("/api/returns")
class ReturnController {
    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<EquipmentReturn> returnEquipment(@RequestBody EquipmentReturn equipmentReturn) {
        return new ResponseEntity<>(returnService.returnEquipment(equipmentReturn), HttpStatus.CREATED);
    }
}

// Equipment Service
@Service
class EquipmentService {
    private final EquipmentRepository equipmentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    public Equipment addEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long id, Equipment equipment) {
        Equipment existing = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        existing.setName(equipment.getName());
        existing.setType(equipment.getType());
        existing.setQuantity(equipment.getQuantity());
        existing.setStatus(equipment.getStatus());
        existing.setLocation(equipment.getLocation());
        return equipmentRepository.save(existing);
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }
}

// Request Service
@Service
class RequestService {
    private final RequestRepository requestRepository;
    private final EquipmentRepository equipmentRepository;
    private final AllocationLogRepository allocationLogRepository;

    public RequestService(RequestRepository requestRepository, EquipmentRepository equipmentRepository,
                          AllocationLogRepository allocationLogRepository) {
        this.requestRepository = requestRepository;
        this.equipmentRepository = equipmentRepository;
        this.allocationLogRepository = allocationLogRepository;
    }

    public EquipmentRequest submitRequest(EquipmentRequest request) {
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("PENDING");
        return requestRepository.save(request);
    }

    public EquipmentRequest approveRequest(Long id) {
        EquipmentRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("APPROVED");
        request = requestRepository.save(request);

        // Update equipment status
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        equipment.setStatus("IN_USE");
        equipment.setQuantity(equipment.getQuantity() - 1);
        equipmentRepository.save(equipment);

        // Log allocation
        AllocationLog log = AllocationLog.builder()
                .equipmentId(request.getEquipmentId())
                .userId(request.getUserId())
                .allocatedAt(LocalDateTime.now())
                .action("ALLOCATED")
                .build();
        allocationLogRepository.save(log);

        return request;
    }

    public EquipmentRequest rejectRequest(Long id) {
        EquipmentRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus("REJECTED");
        return requestRepository.save(request);
    }
}

// Return Service
@Service
class ReturnService {
    private final ReturnRepository returnRepository;
    private final RequestRepository requestRepository;
    private final EquipmentRepository equipmentRepository;

    public ReturnService(ReturnRepository returnRepository, RequestRepository requestRepository,
                         EquipmentRepository equipmentRepository) {
        this.returnRepository = returnRepository;
        this.requestRepository = requestRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public EquipmentReturn returnEquipment(EquipmentReturn equipmentReturn) {
        equipmentReturn.setReturnDate(LocalDateTime.now());
        EquipmentReturn savedReturn = returnRepository.save(equipmentReturn);

        // Update equipment status
        EquipmentRequest request = requestRepository.findById(equipmentReturn.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));
        Equipment equipment = equipmentRepository.findById(request.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        equipment.setStatus(equipmentReturn.getCondition().equals("GOOD") ? "AVAILABLE" : "MAINTENANCE");
        equipment.setQuantity(equipment.getQuantity() + 1);
        equipmentRepository.save(equipment);

        return savedReturn;
    }
}