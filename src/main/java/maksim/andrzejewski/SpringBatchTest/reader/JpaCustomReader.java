package maksim.andrzejewski.SpringBatchTest.reader;

import lombok.RequiredArgsConstructor;
import maksim.andrzejewski.SpringBatchTest.model.Privilege;
import maksim.andrzejewski.SpringBatchTest.model.User;
import maksim.andrzejewski.SpringBatchTest.model.dto.UserPrivilegeDto;
import maksim.andrzejewski.SpringBatchTest.repository.UserRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JpaCustomReader implements ItemReader<UserPrivilegeDto> {

    private final UserRepository userRepository;
    private Slice<User> userSlice;
    private Iterator<UserPrivilegeDto> userPrivilegeDtoIterator;
    private User currentUser;

    @Override
    public UserPrivilegeDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (userSlice == null) {
            userSlice = userRepository.findAll(PageRequest.of(0, 1));
            currentUser = userSlice.getContent().get(0);
            userPrivilegeDtoIterator = createIterator(currentUser);
        } else if (!userPrivilegeDtoIterator.hasNext() && userSlice.hasNext()) {
            userSlice = userRepository.findAll(userSlice.nextPageable());
            currentUser = userSlice.getContent().get(0);
            userPrivilegeDtoIterator = createIterator(currentUser);
        }

        if (userPrivilegeDtoIterator.hasNext()) {
            return userPrivilegeDtoIterator.next();
        }

        return null;
    }

    private Iterator<UserPrivilegeDto> createIterator(User currentUser) {
        final Set<Privilege> privilegeSet = currentUser.getPrivilegeSet();
        if (privilegeSet.isEmpty()) {
            return List.of(UserPrivilegeDto.builder()
                    .userId(currentUser.getId())
                    .userName(currentUser.getUsername())
                    .build()).iterator();
        } else {
            return privilegeSet.stream()
                    .map(privilege -> mapToUserPrivilegeDto(currentUser, privilege))
                    .iterator();
        }
    }

    private UserPrivilegeDto mapToUserPrivilegeDto(User currentUser, Privilege privilege) {
        return UserPrivilegeDto.builder()
                .userId(currentUser.getId())
                .userName(currentUser.getUsername())
                .privilegeName(privilege.getPrivilegeName())
                .build();
    }

}
