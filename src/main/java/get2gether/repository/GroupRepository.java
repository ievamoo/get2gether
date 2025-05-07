package get2gether.repository;

import get2gether.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Boolean existsByName(String groupName);
    Optional<Group> findByName(String groupName);
}
