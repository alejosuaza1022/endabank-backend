package com.endava.endabank.dao;

import com.endava.endabank.model.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantDao extends JpaRepository<Merchant,Integer> {
    Optional<Merchant> findByTaxId(String taxId);
}