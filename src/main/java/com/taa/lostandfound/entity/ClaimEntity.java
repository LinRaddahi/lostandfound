package com.taa.lostandfound.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "claims")
public class ClaimEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "lost_item_id")
    private LostItemEntity lostItem;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public ClaimEntity(UserEntity user, LostItemEntity lostItem, Integer quantity) {
        this.user = user;
        this.lostItem = lostItem;
        this.quantity = quantity;
    }

    @Override
    public Long getId() {
        return id;
    }
}
