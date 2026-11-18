package com.practice.nexora.advisor;

import com.practice.nexora.user.UserAccount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvisorMessageRepository extends JpaRepository<AdvisorMessage, Long> {

    List<AdvisorMessage> findByUserOrderByIdDesc(UserAccount user, Pageable window);

    List<AdvisorMessage> findByUserOrderByIdAsc(UserAccount user);

    @Modifying
    @Query("delete from AdvisorMessage m where m.user = :user")
    int deleteAllOf(@Param("user") UserAccount user);
}
