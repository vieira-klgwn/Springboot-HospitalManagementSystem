package vector.HospitalManagementApplication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.HospitalManagementApplication.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

   Optional <User> findByEmail(String email);



}
