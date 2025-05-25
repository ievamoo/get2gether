package get2gether.repository;

import get2gether.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Boolean existsByName(String groupName);

    Optional<Group> findByName(String groupName);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.id = :id")
    Optional<Group> findByIdWithMembers(@Param("id") Long id);

    @Query("SELECT g.id FROM Group g JOIN g.members m WHERE m.username = :username")
    List<Long> findGroupIdsByMemberUsername(@Param("username") String username);
}
