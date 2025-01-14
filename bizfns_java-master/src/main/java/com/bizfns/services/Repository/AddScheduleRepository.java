package com.bizfns.services.Repository;

import com.bizfns.services.Entity.AddScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;

@Repository
public interface AddScheduleRepository extends JpaRepository<AddScheduleEntity, Integer> {

}
