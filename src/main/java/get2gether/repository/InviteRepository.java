package get2gether.repository;

import get2gether.model.Invite;
import get2gether.model.Type;
import get2gether.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {

    boolean existsByReceiverAndTypeAndTypeId(User receiver, Type type, Long typeId);

}
