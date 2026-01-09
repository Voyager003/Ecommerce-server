package com.ecommerce.domain.member.dao;

import com.ecommerce.domain.member.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByMemberIdOrderByIsDefaultDescCreatedAtDesc(Long memberId);

    int countByMemberId(Long memberId);

    Optional<Address> findByIdAndMemberId(Long id, Long memberId);

    Optional<Address> findByMemberIdAndIsDefaultTrue(Long memberId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.memberId = :memberId AND a.isDefault = true")
    void clearDefaultAddress(@Param("memberId") Long memberId);
}
