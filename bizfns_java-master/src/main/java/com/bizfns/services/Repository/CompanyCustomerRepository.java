package com.bizfns.services.Repository;

import com.bizfns.services.Entity.CompanyCustomerEntity;
import org.json.simple.JSONObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyCustomerRepository extends JpaRepository <CompanyCustomerEntity, Integer>{


   Optional<CompanyCustomerEntity> findByCustomerId(Integer id);


   @Query(value = "SELECT \"COMPANY_ID\"\n" +
           "    FROM \"Bizfns\".\"COMPANY_MASTER\"\n" +
           "    WHERE (\"COMPANY_BACKUP_PHONE_NUMBER\" = :userId OR \"COMPANY_BACKUP_EMAIL\" = :userId) and \"SCHEMA_ID\" = :tenantId", nativeQuery = true)
   List<JSONObject> userWithTenantValidation(String tenantId, String userId);
}
