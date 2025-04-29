package get2gether.service;

import get2gether.dto.GroupRequestDtoNotNeeded;
import get2gether.dto.GroupDto;
import get2gether.manualMapper.ManualGroupMapper;
import get2gether.mapper.GroupMapper;
import get2gether.repository.GroupRepository;
import get2gether.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserService userService;
    private final ManualGroupMapper manualGroupMapper;

    public GroupDto getGroupById(Long id) {
        var foundGroup = groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found by id: " + id));
        return manualGroupMapper.modelToDtoOnGet(foundGroup);
    }

    public GroupDto createGroup(String username, GroupDto groupDto) {
        if (groupRepository.existsByName(groupDto.getName())) {
            throw new EntityExistsException("Group already exists by name: " + groupDto.getName());
        }
        var currentUser = userService.getUserFromDb(username);
        var group = groupMapper.dtoToModelOnGroupCreate(groupDto);
        group.setMembers(Set.of(currentUser));
        group.setAdmin(currentUser);
        var savedGroup = groupRepository.save(group);
        return manualGroupMapper.modelToDtoOnGroupCreate(savedGroup);
    }

}
