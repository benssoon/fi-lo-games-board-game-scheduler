package nl.benzelinsky.filogames.services;

import nl.benzelinsky.filogames.dtos.EventInputDto;
import nl.benzelinsky.filogames.dtos.EventOutputDto;
import nl.benzelinsky.filogames.dtos.PatchEventInputDto;
import nl.benzelinsky.filogames.exceptions.*;
import nl.benzelinsky.filogames.mappers.EventMapper;
import nl.benzelinsky.filogames.models.Event;
import nl.benzelinsky.filogames.models.Game;
import nl.benzelinsky.filogames.models.User;
import nl.benzelinsky.filogames.repositories.EventRepository;
import nl.benzelinsky.filogames.repositories.GameRepository;
import nl.benzelinsky.filogames.repositories.UserRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, GameRepository gameRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    // Create an event
    public EventOutputDto createEvent(EventInputDto dtoIn, String username) {
        Event event = EventMapper.toEntity(dtoIn);
        User host = this.userRepository.findById(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(username));
        Long gameId = dtoIn.gameId;
        Game game = this.gameRepository.findById(gameId)
                        .orElseThrow(() ->
                                new RecordNotFoundException("Game", gameId));
        event.setGame(game);
        game.addEvent(event);

        event.setHost(host);
        host.hostEvent(event);

        if (dtoIn.isHostPlaying) {
            host.joinEvent(event);
            event.addPlayer(host);
        }

        this.eventRepository.save(event);
        this.gameRepository.save(game);
        this.userRepository.save(host);
        return EventMapper.toOutputDto(event);
    }

    // Get all Events
    public List<EventOutputDto> getAllEvents(Long gameId) {
        List<EventOutputDto> allEvents = new ArrayList<>();
        if (gameId != null) {
            if (!this.gameRepository.existsById(gameId)) {
                throw new RecordNotFoundException("Game", gameId);
            }
            this.eventRepository.findEventsByGame_Id(gameId)
                    .forEach(event ->
                            allEvents.add(EventMapper.toOutputDto(event)));
        }
        else {
            this.eventRepository.findAll()
                    .forEach(event ->
                            allEvents.add(EventMapper.toOutputDto(event)));
        }
        return allEvents;
    }

    // Get Event by ID
    public EventOutputDto getEventById(Long id) {
        return EventMapper.toOutputDto(
                this.eventRepository.findById(id)
                        .orElseThrow(() ->
                                new RecordNotFoundException("Event", id)));
    }

    // Update event by ID
    public EventOutputDto updateEventById(Long id, PatchEventInputDto dtoIn) {
        Event toUpdate = this.eventRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", id));

        if (dtoIn.name != null) {
            toUpdate.setName(dtoIn.name);
        }
        if (dtoIn.description != null) {
            toUpdate.setDescription(dtoIn.description);
        }
        if (dtoIn.isHostPlaying != null) {
            toUpdate.setHostPlaying(dtoIn.isHostPlaying);
        }
        if (dtoIn.definitiveTime != null) {
            toUpdate.setDefinitiveTime(dtoIn.definitiveTime);
        }
        if (dtoIn.location != null) {
            toUpdate.setLocation(dtoIn.location);
        }
        if (dtoIn.possibleTimes != null) {
            toUpdate.setPossibleTimes(dtoIn.possibleTimes);
        }

        this.eventRepository.save(toUpdate);

        return EventMapper.toOutputDto(toUpdate);
    }

    // Delete event by ID
    public String deleteEventById(Long id) {
        Event toDelete = this.eventRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", id));
        // Iterate over a copy of toDelete.players to avoid a bug when deleting players from the same collection we're iterating over.
        new ArrayList<>(toDelete.getPlayers())
                .forEach(player ->
                        this.removePlayer(player.getUsername(), toDelete.getId()));
        this.eventRepository.delete(toDelete);
        return "Event with id " + id + " has been deleted.";
    }

    // Delete all events (besides default event)
    public String deleteAllEvents() {
        Long protectedId = 1L;
        List<Long> toDeleteIds = new ArrayList<>();
        // Create a list of all IDs except for the first one.
        this.eventRepository
                .findAll()
                .forEach(event ->
                        toDeleteIds.add(event.getId()));
        toDeleteIds.remove(protectedId); // Remove ID 1 from the list of all IDs.

        System.out.println();
        toDeleteIds
                .forEach(id ->
                        System.out.println(this.deleteEventById(id)));
        System.out.println();
        return "All events except Event with id " + protectedId + " have been deleted.";
    }

    // Couple Event with Game
    public EventOutputDto assignGameToEvent(Long gameId, Long eventId) {
        Game game = this.gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Game", gameId));
        Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", eventId));
        if (game.getActiveEvents().contains(event)) {
            throw new GameAlreadyAssignedToEventException(game, event);
        }
        game.removeEvent(event);
        event.setGame(game);
        game.addEvent(event);
        this.eventRepository.save(event);
        this.gameRepository.save(game);

        return EventMapper.toOutputDto(event);
    }

    // TODO add endpoint
    // Add host to Event
    public EventOutputDto assignHostToEvent(String username, Long eventId) {
        User host = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", eventId));
        if (host.getHostedEvents().contains(event)) {
            throw new AlreadyHostingException(host, event);
        }
        event.getHost().stopHostingEvent(event);
        event.setHost(host);
        host.hostEvent(event);

        return EventMapper.toOutputDto(event);
    }

    // Add player to Event
    public EventOutputDto addPlayer(String username, Long eventId) {
        Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", eventId));
        User player = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        if (event.getPlayers().contains(player)) {
            throw new UserAlreadyJoinedEventException(username, eventId);
        }
        if (event.isFull()) {
            throw new EventFullException(event.getName());
        }
        if (player == event.getHost()) {
            event.setHostPlaying(true);
        }
        event.addPlayer(player);
        player.joinEvent(event);

        return EventMapper.toOutputDto(event);
    }

    public EventOutputDto removePlayer(String username, Long eventId) {
        Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", eventId));
        User player = this.userRepository.findById(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(username));
        if (event.getPlayers().contains(player)) {
            if (player == event.getHost()) {
                event.setHostPlaying(false);
            }
            event.removePlayer(player);
            player.leaveEvent(event);
        }
        else {
            // player is not in event
            throw new NotAPlayerException(username, eventId);
        }

        return EventMapper.toOutputDto(event);
    }

    public String closeEvent(Long id) {
        Event event = this.eventRepository.findById(id)
                .orElseThrow(() ->
                        new RecordNotFoundException("Event", id));
        event.getGame().getStats().incrementPlays(event.getPlayers().size());
        event.setClosed(true);
        return "Event with id " + id + " has been closed.";
    }
}
