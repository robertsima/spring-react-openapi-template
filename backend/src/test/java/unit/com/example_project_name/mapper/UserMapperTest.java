package unit.com.example_project_name.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example_project_name.generated.model.UserDTO;
import com.example_project_name.mapper.UserMapper;
import com.example_project_name.model.Users;

// Placeholder: one test per mapper keeps contract (generated DTO) <-> entity drift visible.
class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void mapsEntityToGeneratedDto() {
        Users user = new Users();
        user.setName("Alice Johnson");

        UserDTO dto = mapper.toDto(user);

        assertThat(dto.getName()).isEqualTo("Alice Johnson");
    }
}
