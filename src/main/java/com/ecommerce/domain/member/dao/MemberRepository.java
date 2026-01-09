package com.ecommerce.domain.member.dao;

import com.ecommerce.domain.member.domain.Member;
import com.ecommerce.domain.member.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.status = :status " +
           "AND m.withdrawnAt > :withdrawnAfter")
    Optional<Member> findRecentlyWithdrawnByEmail(
            @Param("email") String email,
            @Param("status") MemberStatus status,
            @Param("withdrawnAfter") LocalDateTime withdrawnAfter
    );

    @Query("SELECT m FROM Member m WHERE m.status = 'ACTIVE'")
    List<Member> findByStatusActive();
}
