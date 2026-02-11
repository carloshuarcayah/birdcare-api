package pe.com.birdcare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.birdcare.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Page<User>findAllByActiveTrue(Pageable pageable);
    Page<User>findAllByNameContainingIgnoreCase(Pageable pageable,String name);

    Optional<User> findByEmail(String email);
}
