package com.taa.lostandfound.repository;

import com.taa.lostandfound.entity.LostItemEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LostItemRepository extends JpaRepository<LostItemEntity, Long> {
    LostItemEntity findByItemNameAndPlace(@NonNull String itemName, @NonNull String place);
}
