package maksim.andrzejewski.SpringBatchTest.mapper;

import maksim.andrzejewski.SpringBatchTest.model.dto.UserPrivilegeDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.swing.tree.TreePath;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserPrivilegeRowMapper implements RowMapper<UserPrivilegeDto> {

    @Override
    public UserPrivilegeDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UserPrivilegeDto.builder()
                .userId(rs.getLong("user_id"))
                .userName(rs.getString("username"))
                .privilegeName(rs.getString("privilege_name"))
                .build();
    }
}
