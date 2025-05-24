package get2gether.repository;

import get2gether.model.Invite;
import get2gether.model.Type;
import get2gether.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteRepository extends JpaRepository<Invite, Long> {
    boolean existsByReceiverAndTypeAndTypeId(User receiver, Type type, Long typeId);

    List<Invite> findByTypeAndTypeId(Type type, Long typeId);

    Optional<Invite> findByReceiverAndTypeAndTypeId(User receiver, Type type, Long typeId);

}
