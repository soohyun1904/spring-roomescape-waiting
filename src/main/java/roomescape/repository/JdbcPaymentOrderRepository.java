package roomescape.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.payment.PaymentOrder;
import roomescape.domain.payment.PaymentOrderRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcPaymentOrderRepository implements PaymentOrderRepository {
    private static final RowMapper<PaymentOrder> ROW_MAPPER = (rs, rowNum) ->
            PaymentOrder.load(
                    rs.getLong("id"),
                    rs.getString("order_id"),
                    rs.getLong("reservation_id"),
                    rs.getLong("amount"),
                    rs.getString("payment_key"));

    private static final String BASE_SQL = "SELECT id, order_id, reservation_id, amount, payment_key FROM payment_order";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcPaymentOrderRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("payment_order")
                .usingGeneratedKeyColumns("id")
                .usingColumns("order_id", "reservation_id", "amount");
    }

    public PaymentOrder save(PaymentOrder order) {
        MapSqlParameterSource params = new MapSqlParameterSource("order_id", order.getOrderId())
                .addValue("reservation_id", order.getReservationId())
                .addValue("amount", order.getAmount());

        long generatedKey = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return order.withId(generatedKey);
    }

    public Optional<PaymentOrder> findByOrderId(String orderId) {
        List<PaymentOrder> result = jdbcTemplate.query(
                BASE_SQL + " WHERE order_id = :orderId",
                new MapSqlParameterSource("orderId", orderId),
                ROW_MAPPER);
        return result.stream().findFirst();
    }

    public void updatePaymentKey(Long id, String paymentKey) {
        MapSqlParameterSource params = new MapSqlParameterSource("id", id)
                .addValue("paymentKey", paymentKey);
        jdbcTemplate.update("UPDATE payment_order SET payment_key = :paymentKey WHERE id = :id", params);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("DELETE FROM payment_order WHERE id = :id", new MapSqlParameterSource("id", id));
    }
}
