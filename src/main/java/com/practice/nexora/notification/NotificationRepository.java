package com.practice.nexora.notification;

import com.practice.nexora.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDescIdDesc(UserAccount user);

    @Modifying
    @Query("update Notification n set n.readAt = :now where n.user = :user and n.readAt is null")
    int markAllRead(@Param("user") UserAccount user, @Param("now") Instant now);

    @Modifying
    @Query(value = """
            insert into notifications (user_id, price_change_id, product_slug, message, created_at)
            select w.user_id,
                   ph.id,
                   p.slug,
                   'Price drop: ' || p.name || ' (' || v.sku || ') is now '
                       || ph.new_price_amount || ' ' || ph.new_price_currency
                       || ', was ' || ph.old_price_amount || ' ' || ph.old_price_currency,
                   now()
            from price_history ph
            join product_variants v on v.id = ph.variant_id
            join products p         on p.id = v.product_id
            join wishlist_items w   on w.product_id = p.id
            where ph.new_price_amount < ph.old_price_amount
              and ph.changed_at >= :since
              and ph.changed_at >= w.added_at
            on conflict (user_id, price_change_id) do nothing
            """, nativeQuery = true)
    int createPriceDropNotifications(@Param("since") Instant since);
}
