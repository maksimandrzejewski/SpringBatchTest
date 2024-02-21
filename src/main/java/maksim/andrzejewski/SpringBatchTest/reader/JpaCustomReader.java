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

@Component
@RequiredArgsConstructor
public class JpaCustomReader implements ItemReader<UserPrivilegeDto> {

    private final UserRepository userRepository;
    private Slice<User> userSlice;
    private Iterator<Privilege> privilegeIterator;
    private User currentUser;

    @Override
    public UserPrivilegeDto read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (userSlice == null) {
            userSlice = userRepository.findAll(PageRequest.of(0, 1));
            currentUser = userSlice.getContent().get(0);
            privilegeIterator = currentUser.getPrivilegeSet().iterator();
        }

        if (userSlice.hasNext() || (privilegeIterator != null && privilegeIterator.hasNext())) {
            if (privilegeIterator == null || !privilegeIterator.hasNext()) {
                userSlice = userRepository.findAll(userSlice.nextPageable());
                currentUser = userSlice.getContent().get(0);
                privilegeIterator = currentUser.getPrivilegeSet().iterator();
            }
            if (!privilegeIterator.hasNext()) {
                return UserPrivilegeDto.builder()
                        .userId(currentUser.getId())
                        .userName(currentUser.getUsername())
                        .build();
            }
            final Privilege privilege = privilegeIterator.next();
            return UserPrivilegeDto.builder()
                    .userId(currentUser.getId())
                    .userName(currentUser.getUsername())
                    .privilegeName(privilege.getPrivilegeName())
                    .build();
        }


        return null;
    }

}
