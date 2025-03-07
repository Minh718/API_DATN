package com.shop.fashion.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shop.fashion.entities.UserVoucher;
import com.shop.fashion.entities.UserVoucherId;
import com.shop.fashion.entities.Voucher;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, UserVoucherId> {

    @Query("SELECT uv.voucher FROM UserVoucher uv WHERE uv.user.id = :idUser AND uv.voucher.startDate <= CURRENT_TIMESTAMP AND uv.voucher.endDate >= CURRENT_TIMESTAMP AND uv.voucher.isActive = true AND uv.isUsed = false")
    Page<Voucher> findAllByIdUserAndStillApply(@Param("idUser") String idUser, Pageable pageable);
}