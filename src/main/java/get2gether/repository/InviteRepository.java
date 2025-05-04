package get2gether.repository;

import get2gether.model.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
}
