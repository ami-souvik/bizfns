package com.bizfns.services.Repository;

import com.bizfns.services.Entity.SubscrptionPlanMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscrptionPlanMasterRepository extends JpaRepository<SubscrptionPlanMasterEntity, Integer > {


}


