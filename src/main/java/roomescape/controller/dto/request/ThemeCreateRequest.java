package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ThemeCreateRequest {
    @NotNull(message = "이름은 필수로 입력해야 합니다")
    private final String name;

    @NotNull(message = "설명은 필수로 입력해야 합니다")
    private final String description;

    @NotNull(message = "URL은 필수로 입력해야 합니다")
    private final String thumbnailUrl;

    @NotNull(message = "가격은 필수로 입력해야 합니다")
    @Positive(message = "가격은 양수여야 합니다.")
    private final Long price;

    public ThemeCreateRequest(String name, String description, String thumbnailUrl, Long price) {
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Long getPrice() {
        return price;
    }
}
