package cz.muni.ics.kypo.userandgroup.persistence;

import cz.muni.ics.kypo.userandgroup.dbmodel.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByScreenName(String screenName);

    @Query("SELECT u.screenName FROM User u WHERE u.id = :userId")
    String getScreenName(@Param("userId") Long id);

    @Query("SELECT CASE WHEN u.externalId IS NULL THEN true ELSE false END FROM User u WHERE u.id = :userId")
    boolean isUserInternal(@Param("userId") Long id);
}
