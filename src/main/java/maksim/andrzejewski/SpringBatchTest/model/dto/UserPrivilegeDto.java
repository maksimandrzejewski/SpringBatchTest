package maksim.andrzejewski.SpringBatchTest.model.dto;

import lombok.Builder;

@Builder
public record UserPrivilegeDto(
        Long userId,
        String userName,
        String privilegeName
) {
}
