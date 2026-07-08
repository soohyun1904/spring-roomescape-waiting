package roomescape.domain.theme;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.domain.RoomEscapeException;

import java.util.stream.Stream;

public class ThemeTest {
    static Stream<Arguments> nullCases() {
        ThemeName name = new ThemeName("공포의 방");
        String description = "무서운 테마";
        ThumbnailUrl thumbnailUrl = new ThumbnailUrl("https://zeze.com/thumb.jpg");
        ThemePrice price = new ThemePrice(10000L);

        return Stream.of(
                Arguments.of(null, description, thumbnailUrl, price),
                Arguments.of(name, null, thumbnailUrl, price),
                Arguments.of(name, description, null, price),
                Arguments.of(name, description, thumbnailUrl, null)
        );
    }

    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(ThemeName themeName, String description, ThumbnailUrl thumbnailUrl, ThemePrice price) {
        Assertions.assertThatThrownBy(() -> Theme.create(themeName, description, thumbnailUrl, price))
                .isInstanceOf(RoomEscapeException.class);
    }
}
