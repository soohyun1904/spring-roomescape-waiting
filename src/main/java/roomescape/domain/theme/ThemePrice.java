package roomescape.domain.theme;

import java.util.Objects;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.require;
import static roomescape.domain.DomainPreconditions.requireNonNull;

public class ThemePrice {
    private final Long value;

    public ThemePrice(Long value) {
        requireNonNull(value, INVALID_INPUT, "테마 가격은 비어있을 수 없습니다.");
        require(value > 0, INVALID_INPUT, "테마 가격은 양수여야 합니다.");
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemePrice that = (ThemePrice) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
