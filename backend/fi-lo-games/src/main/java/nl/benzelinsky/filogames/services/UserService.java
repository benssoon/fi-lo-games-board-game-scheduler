package nl.benzelinsky.filogames.services;

import nl.benzelinsky.filogames.dtos.*;
import nl.benzelinsky.filogames.exceptions.*;
import nl.benzelinsky.filogames.mappers.UserMapper;
import nl.benzelinsky.filogames.models.Role;
import nl.benzelinsky.filogames.models.User;
import nl.benzelinsky.filogames.repositories.UserRepository;
import nl.benzelinsky.filogames.utils.RandomStringGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.userRepository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // Create user
    public ShortUserOutputDto createUser(UserInputDto userInputDto) {
        if (this.userRepository.existsUserByUsername(userInputDto.username)) {
            throw new UsernameUnavailableException(userInputDto.username);
        }
        if (this.userRepository.existsUserByEmailAddress(userInputDto.emailAddress)) {
            throw new UserAlreadyExistsException("email address", userInputDto.emailAddress);
        }
        if (this.userRepository.existsUserByTelephoneNumber(userInputDto.telephoneNumber)) {
            throw new UserAlreadyExistsException("telephone number", userInputDto.telephoneNumber);
        }
        //userInputDto.apiKey = RandomStringGenerator.generateAlphaNumeric(20); // TODO unnecessary?
        userInputDto.password = passwordEncoder.encode(userInputDto.password);
        User newUser = UserMapper.toEntity(userInputDto);
        for (String role : userInputDto.roles) {
            final String fullRoleName = "ROLE_" + role.toUpperCase();
            newUser.addRole(new Role(newUser.getUsername(), fullRoleName));
        }
        this.userRepository.save(newUser);
        return UserMapper.toShortDto(newUser);
    }

    // Get all users
    public List<TinyUserOutputDto> getAllUsers() {
        List<TinyUserOutputDto> allUsers = new ArrayList<>();
        this.userRepository.findAll()
                .forEach(user ->
                        allUsers.add(UserMapper.toTinyDto(user)));
        return allUsers;
    }

    // Get user by username
    public Object getUser(String username, boolean isSelf) {
        User user = this.userRepository.findById(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(username));
        if (isSelf) {
            return UserMapper.toShortDto(user);
        } else {
            return UserMapper.toTinyDto(user);
        }
    }

    public UserOutputDto getUserWithPassword(String username) {
        return UserMapper.toFullDto(
                this.userRepository.findById(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(username)));
    }
    
    // Update user by username
    public ShortUserOutputDto updateWholeUser(String username, UserInputDto dtoIn) {
        User toUpdate = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));

        if (!dtoIn.username.equals(username)) { // Check that the username is not different being updated in the dto.
            throw new MayNotChangeUsernameException();
        }

        // Doing this with a mapper wouldn't work, because then the record would get a different id.
        toUpdate.setPassword(passwordEncoder.encode(dtoIn.password));
        toUpdate.setName(dtoIn.name);
        toUpdate.setEmailAddress(dtoIn.emailAddress);
        toUpdate.setTelephoneNumber(dtoIn.telephoneNumber);
        toUpdate.setAge(dtoIn.age);
        toUpdate.setArea(dtoIn.area);
        toUpdate.setAddress(dtoIn.address);

        return UserMapper.toShortDto(toUpdate);
    }

    // Partially update user
    public ShortUserOutputDto updateUser(String username, PatchUserInputDto dtoIn) {
        User toUpdate = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));

        if (dtoIn.password != null) {
            toUpdate.setPassword(dtoIn.password);
        }
        if (dtoIn.name != null) {
            toUpdate.setName(dtoIn.name);
        }
        if (dtoIn.emailAddress != null) {
            toUpdate.setEmailAddress(dtoIn.emailAddress);
        }
        if (dtoIn.telephoneNumber != null) {
            toUpdate.setTelephoneNumber(dtoIn.telephoneNumber);
        }
        if (dtoIn.area != null) {
            toUpdate.setArea(dtoIn.area);
        }
        if (dtoIn.address != null) {
            toUpdate.setAddress(dtoIn.address);
        }

        return UserMapper.toShortDto(toUpdate);
    }

    // Get User roles
    public Set<Role> getRoles(String username) {
        User user = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        return user.getRoles();
    }

    public void addRole(String username, String roleName) {
        final String fullRoleName = "ROLE_" + roleName.toUpperCase();
        User user = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        user.getRoles().forEach(role -> {
            if (role.getRole().equals(fullRoleName)) {
                throw new AlreadyHasRoleException(username, fullRoleName);
            }
        });
        user.addRole(new Role(username, fullRoleName));
        this.userRepository.save(user);
    }

    public void removeRole(String username, String role, String currentUser) {
        User user = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username)); // User does not have specified role
        Role roleToRemove = user.getRoles().stream().filter((a) -> a.getRole().equalsIgnoreCase("ROLE_"+role)).findAny()
                        .orElseThrow(() ->
                                new RoleNotFoundException(username, role));
        if (currentUser.equals(username) && user.getRoles().contains(roleToRemove) && roleToRemove.getRole().equals("ROLE_ADMIN")) { // Current user cannot remove their own admin role.
            throw new AdminCannotRemoveOwnAdminRoleException();
        }
        if (roleToRemove.getRole().equals("ROLE_USER") && !(user.getHostedEvents().isEmpty() && user.getJoinedEvents().isEmpty())) { // Cannot remove user role if the user is still participating in or hosting events.
            throw new CannotRemoveUserRoleFromActiveUserException();
        }
        user.removeRole(roleToRemove);
        this.userRepository.save(user);
    }

    // Delete user by username
    public String deleteUser(String username) {
        User toDelete = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        if (!toDelete.getHostedEvents().isEmpty()) {
            Map<Long, String> eventIds = new HashMap<>();
            toDelete.getHostedEvents()
                    .forEach(event -> eventIds.put(event.getId(), event.getName()));
            throw new HasActiveEventsException(username, eventIds);
        }
        else {
            this.userRepository.deleteById(username);
        }
        return "User " + toDelete.getUsername() + " has been deleted.";
    }
}